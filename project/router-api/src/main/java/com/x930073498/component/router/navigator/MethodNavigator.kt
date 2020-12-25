package com.x930073498.component.router.navigator

import android.os.Bundle
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.impl.ResultHandler
import com.x930073498.component.router.thread.IThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

interface MethodNavigator : ParameterProvider, Navigator {
    companion object {
        internal fun create(
            target: Target.MethodTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): MethodNavigator {
            return object : MethodNavigator {

                private var methodInvokerRef = WeakReference<MethodInvoker>(null)
                override suspend fun invoke(): Any? {
                    val invoker = getMethodInvoker()
                    return when (target.action.thread) {
                        IThread.UI -> {
                            if (isMainThread) {
                                invoker.invoke(contextHolder, bundle)
                            }else{
                                withContext(Dispatchers.Main.immediate){
                                    invoker.invoke(contextHolder, bundle)
                                }
                            }
                        }
                        IThread.WORKER ->{
                            if (isMainThread){
                                withContext(Dispatchers.IO){
                                    invoker.invoke(contextHolder, bundle)
                                }
                            }else{
                                invoker.invoke(contextHolder, bundle)
                            }
                        }
                        IThread.ANY -> invoker.invoke(contextHolder, bundle)
                    }

                }

                override suspend fun getMethodInvoker(): MethodInvoker {
                    var invoker = methodInvokerRef.get()
                    if (invoker != null) return invoker
                    val factory = target.action.factory()
                    invoker = factory.create(contextHolder, target.targetClazz, bundle)
                    return invoker.apply {
                        methodInvokerRef = WeakReference(this)
                    }
                }

                override fun getBundle(): Bundle {
                    return bundle
                }

                override fun getContextHolder(): ContextHolder {
                    return contextHolder
                }

                override suspend fun navigate(

                ): Any? {
                    return invoke()
                }

            }
        }
    }

    suspend fun invoke(): Any?
    suspend fun getMethodInvoker(): MethodInvoker
}