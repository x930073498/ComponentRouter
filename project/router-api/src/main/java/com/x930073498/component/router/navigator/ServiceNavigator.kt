package com.x930073498.component.router.navigator

import android.os.Bundle
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.ResultHandler
import java.lang.ref.WeakReference

interface ServiceNavigator : ParameterProvider, Navigator {
    suspend fun getService(): IService
    suspend fun <T : IService> getInstanceService(clazz: Class<T>): T

    companion object {
        internal fun create(
            target: Target.ServiceTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): ServiceNavigator {
            return object : ServiceNavigator {

                private var serviceRef = WeakReference<IService>(null)
                override suspend fun getService(): IService {
                    var service = serviceRef.get()
                    if (service != null) return service
                    val factory = target.action.factory()
                    service = factory.create(contextHolder, target.targetClazz, bundle)
                    service.init(contextHolder, bundle)
                    return service.apply {
                        serviceRef = WeakReference(service)
                    }
                }

                override suspend fun <T : IService> getInstanceService(clazz: Class<T>): T {
                    return runCatching { getService() as T }.getOrElse {
                        throw RuntimeException("目标${serviceRef.get()} 不能强转为$clazz")
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
                    val service = getService()
                    val action = target.action
                    return if (action.autoInvoke()) {
                        service.invoke()
                    } else service

                }

            }
        }
    }
}