package com.x930073498.router.impl

import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target

interface InterceptorActionDelegate:ActionDelegate {

    suspend fun factory(): Factory

    suspend fun target(): Target.InterceptorTarget


    interface Factory {
        suspend fun create(contextHolder: ContextHolder, clazz: Class<*>): RouterInterceptor
    }
}