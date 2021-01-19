package com.x930073498.component.router.navigator

import android.os.Bundle
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.thread.IThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal class MethodNavigatorImpl(
    private val listenable: ResultListenable<MethodNavigatorParams>,

    ) : MethodNavigator {
    private var methodInvokerRef = WeakReference<MethodInvoker>(null)
    override fun invoke(): ResultListenable<Any?> {
        return getMethodInvoker().map { invoker ->
            listenable.await().run {
                when (target.action.thread) {
                    IThread.UI -> {
                        withContext(Dispatchers.Main.immediate) {
                            invoker.invoke(contextHolder, bundle)
                        }
                    }
                    IThread.WORKER -> {
                        withContext(Dispatchers.IO) {
                            invoker.invoke(contextHolder, bundle)
                        }
                    }
                    IThread.ANY -> invoker.invoke(contextHolder, bundle)
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

            ): MethodNavigator {
            return MethodNavigatorImpl(listenable)
        }
    }

    fun invoke(): ResultListenable<Any?>
    fun getMethodInvoker(): ResultListenable<MethodInvoker>
}