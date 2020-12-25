package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.router.action.ContextHolder

interface ServiceActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.SERVICE
    }

     fun factory(): Factory

    fun autoInvoke(): Boolean

    interface Factory {
         fun create(contextHolder: ContextHolder, clazz: Class<*>, bundle: Bundle):IService

    }

}