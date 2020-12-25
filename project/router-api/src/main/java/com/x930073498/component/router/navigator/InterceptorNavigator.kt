package com.x930073498.component.router.navigator

import android.os.Bundle
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.NavigateInterceptor
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.impl.ResultHandler

interface InterceptorNavigator : Navigator {
    companion object {
        internal fun create(
            target: Target.InterceptorTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): InterceptorNavigator {
            return object : InterceptorNavigator {
                override suspend fun navigate(
                ): Any? {
                    val factory = target.action.factory()
                    return factory.create(contextHolder, target.targetClazz)
                }

            }
        }
    }
}