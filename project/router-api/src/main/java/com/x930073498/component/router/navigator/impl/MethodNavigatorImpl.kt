package com.x930073498.component.router.navigator.impl

import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.navigator.MethodNavigator
import com.x930073498.component.router.navigator.MethodNavigatorParams
import com.x930073498.component.router.navigator.NavigatorOption
import com.x930073498.component.router.navigator.NavigatorResult
import com.x930073498.component.router.thread.IThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal class MethodNavigatorImpl(
    private val listenable: ResultListenable<MethodNavigatorParams>,
    private val methodNavigatorOption: NavigatorOption.MethodNavigatorOption,

    ) : MethodNavigator {

    private class DelegateMethodInvoker(
        private val delegate: MethodInvoker,
        private val thread: IThread
    ) : MethodInvoker {
        override suspend fun invoke(): Any? {
            return when (thread) {
                IThread.UI -> {
                    withContext(Dispatchers.Main.immediate) {
                        delegate.invoke()
                    }
                }
                IThread.WORKER -> {
                    withContext(Dispatchers.IO) {
                        delegate.invoke()
                    }
                }
                IThread.ANY -> delegate.invoke()
            }
        }

    }

    private var methodInvokerRef = WeakReference<MethodInvoker>(null)
    override fun invoke(): ResultListenable<Any?> {
        return getMethodInvoker().map { invoker ->
            invoker.invoke()
        }


    }

    override fun getMethodInvoker(): ResultListenable<MethodInvoker> {
        return listenable.map {
            it.run {
                var invoker = methodInvokerRef.get()
                if (invoker != null) return@map invoker
                val action = target.action
                val factory = action.factory()
                invoker = DelegateMethodInvoker(
                    factory.create(contextHolder, target.targetClazz, bundle),
                    methodNavigatorOption.thread ?: action.thread
                )
                invoker.apply {
                    methodInvokerRef = WeakReference(this)
                }
            }
        }
    }


    override fun navigate(): ResultListenable<NavigatorResult> {
        return invoke().map {
            NavigatorResult.METHOD(it)
        }
    }

}
