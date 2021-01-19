package com.x930073498.component.router.navigator

import android.os.Bundle
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.response.RouterResponse
import java.lang.ref.WeakReference


internal class ServiceNavigatorImpl(
    private val listenable: ResultListenable<ServiceNavigatorParams>,

    ) : ServiceNavigator {

    private val serviceLazy by lazy {
        listenable.map {
            with(it) {
                val service = serviceRef.get()
                if (service != null) return@with service
                val factory = target.action.factory()
                factory.create(contextHolder, target.targetClazz, bundle).apply {
                    init(contextHolder, bundle)
                    serviceRef = WeakReference(service)
                }

            }
        }
    }
    private var serviceRef = WeakReference<IService>(null)
    override suspend fun getService(): IService {
        return serviceLazy.await()
    }

    override suspend fun <T : IService> getInstanceService(clazz: Class<T>): T {
        return runCatching { getService() as T }.getOrElse {
            throw RuntimeException("目标${serviceRef.get()} 不能强转为$clazz")
        }
    }


    override fun navigate(): ResultListenable<NavigatorResult> {
        return serviceLazy.map {
            val action=listenable.await().target.action
            if (action.autoInvoke()) {
                NavigatorResult.SERVICE(true, it, it.invoke())
            } else {
                NavigatorResult.SERVICE(false, it, null)
            }
        }

    }


}

interface ServiceNavigator : Navigator {
    suspend fun getService(): IService
    suspend fun <T : IService> getInstanceService(clazz: Class<T>): T


    companion object {
        internal fun create(
            listenable: ResultListenable<ServiceNavigatorParams>): ServiceNavigator {
            return ServiceNavigatorImpl(listenable)
        }
    }
}