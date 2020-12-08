package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target

interface InterceptorActionDelegate:ActionDelegate {
    override fun type(): ActionType {
        return ActionType.INTERCEPTOR
    }
    suspend fun factory(): Factory
    override fun inject(bundle: Bundle, target: Any) {

    }


    interface Factory {
        suspend fun create(contextHolder: ContextHolder, clazz: Class<*>): RouterInterceptor
    }
}