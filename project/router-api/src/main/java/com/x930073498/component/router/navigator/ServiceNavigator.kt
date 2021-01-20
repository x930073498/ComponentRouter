package com.x930073498.component.router.navigator

import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.navigator.impl.ServiceNavigatorImpl


interface ServiceNavigator : Navigator {
    fun getService(): ResultListenable<IService>
    fun <T : IService> getInstanceService(clazz: Class<T>): ResultListenable<T>


    companion object {
        internal fun create(
            listenable: ResultListenable<ServiceNavigatorParams>,
            navigatorOption: NavigatorOption
        ): ServiceNavigator {
            val serviceNavigatorOption = navigatorOption as? NavigatorOption.ServiceNavigatorOption
                ?: NavigatorOption.ServiceNavigatorOption()
            return ServiceNavigatorImpl(listenable, serviceNavigatorOption)
        }
    }
}