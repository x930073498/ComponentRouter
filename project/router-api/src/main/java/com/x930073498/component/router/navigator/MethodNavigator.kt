package com.x930073498.component.router.navigator

import com.x930073498.component.router.action.*
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.thread.IThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal class MethodNavigatorImpl(
    private val listenable: ResultListenable<MethodNavigatorParams>,
    private val methodNavigatorOption: NavigatorOption.MethodNavigatorOption,

    ) : MethodNavigator {
    private var methodInvokerRef = WeakReference<MethodInvoker>(null)
    override fun invoke(): ResultListenable<Any?> {
        return getMethodInvoker().map { invoker ->
            listenable.await().run {
                when (target.action.thread) {
                    IThread.UI -> {
                        withContext(Dispatchers.Main.immediate) {
                            invoker.invoke()
                        }
                    }
                    IThread.WORKER -> {
                        withContext(Dispatchers.IO) {
                            invoker.invoke()
                        }
                    }
                    IThread.ANY -> invoker.invoke()
                }
            }

        }


    }

    override fun getMethodInvoker(): ResultListenable<MethodInvoker> {
        return listenable.map {
            it.run {
                var invoker = methodInvokerRef.get()
                if (invoker != null) return@map invoker
                val factory = target.action.factory()
                invoker = factory.create(contextHolder, target.targetClazz, bundle)
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

interface MethodNavigator : Navigator {
    companion object {
        internal fun create(
            listenable: ResultListenable<MethodNavigatorParams>,
            navigatorOption: NavigatorOption,
        ): MethodNavigator {
            val methodNavigatorOption = navigatorOption as? NavigatorOption.MethodNavigatorOption
                ?: NavigatorOption.MethodNavigatorOption()
            return MethodNavigatorImpl(listenable, methodNavigatorOption)
        }
    }

    fun invoke(): ResultListenable<Any?>
    fun getMethodInvoker(): ResultListenable<MethodInvoker>
}