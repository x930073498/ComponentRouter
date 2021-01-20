package com.x930073498.component.router.navigator

import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.navigator.impl.MethodNavigatorImpl


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