package com.x930073498.component.router.navigator.impl

import android.os.Bundle
import androidx.collection.arrayMapOf
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.navigator.NavigatorOption
import com.x930073498.component.router.navigator.NavigatorResult
import com.x930073498.component.router.navigator.ServiceNavigator
import com.x930073498.component.router.navigator.ServiceNavigatorParams
import com.x930073498.component.router.response.RouterResponse
import java.lang.ref.WeakReference


internal class ServiceNavigatorImpl(
    private val listenable: ResultListenable<ServiceNavigatorParams>,
    private val navigatorOption: NavigatorOption.ServiceNavigatorOption
) : ServiceNavigator {

    companion object {
        private val singletonMap = arrayMapOf<Class<*>, IService>()
    }

    private val serviceLazy: ResultListenable<IService> by lazy {
        listenable.map {
            with(it) {
                var service = serviceRef.get()
                if (service != null) return@with service
                val currentSingleton = navigatorOption.singleton ?: target.isSingleTon
                if (currentSingleton) {
                    service = singletonMap[target.targetClazz]
                }
                if (service != null) return@with service
                val factory = target.action.factory()
                factory.create(contextHolder, target.targetClazz, bundle).apply {
                    target.action.inject(bundle, this)
                    init(contextHolder, bundle)
                    serviceRef = WeakReference(this)
                    if (target.isSingleTon) {
                        singletonMap[target.targetClazz] = this
                    }
                }

            }
        }
    }
    private var serviceRef = WeakReference<IService>(null)
    override fun getService(): ResultListenable<IService> {
        return serviceLazy
    }

    override fun <T : IService> getInstanceService(clazz: Class<T>): ResultListenable<T> {
        return serviceLazy.map { clazz.cast(it) as T }
    }


    override fun navigate(): ResultListenable<NavigatorResult> {
        return serviceLazy.map {
            val action = listenable.await().target.action
            val currentAutoInvoke = navigatorOption.autoInvoke ?: action.autoInvoke()
            if (currentAutoInvoke) {
                NavigatorResult.SERVICE(true, it, it.invoke())
            } else {
                NavigatorResult.SERVICE(false, it, null)
            }
        }

    }


}


suspend inline fun ServiceNavigator.service(): IService {
    return getService().await()
}

inline fun <reified T : IService> ServiceNavigator.getInstanceService(): ResultListenable<T> {
    return getInstanceService(T::class.java)
}

suspend inline fun <reified T : IService> ServiceNavigator.instanceService(): T {
    return getInstanceService<T>().await()
}