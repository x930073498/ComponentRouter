package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target

interface ServiceActionDelegate : ActionDelegate {
    fun inject(bundle: Bundle, target: IService)

    suspend fun factory(): Factory

    suspend fun target(): Target.ServiceTarget
    fun autoInvoke(): Boolean

    interface Factory {
        suspend fun create(contextHolder: ContextHolder, clazz: Class<*>, bundle: Bundle):IService?
    }

}