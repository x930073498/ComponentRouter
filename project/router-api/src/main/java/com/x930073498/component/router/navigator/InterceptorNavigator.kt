package com.x930073498.component.router.navigator

import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.cast
import com.x930073498.component.router.coroutines.map

internal fun InterceptorNavigator.interceptorNavigate()=navigate().cast<NavigatorResult,NavigatorResult.INTERCEPTOR>()
interface InterceptorNavigator : Navigator {
    companion object {
        internal fun create(
            listenable: ResultListenable<InterceptorNavigatorParams>,
        ): InterceptorNavigator {
            return object : InterceptorNavigator {
                override  fun navigate(
                ): ResultListenable<NavigatorResult> {
                 return listenable.map {
                      val factory =it. target.action.factory()
                      NavigatorResult.INTERCEPTOR(factory.create(it.contextHolder, it.target.targetClazz))
                  }
                }

            }
        }
    }
}