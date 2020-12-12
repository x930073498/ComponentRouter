package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target

interface MethodActionDelegate : ActionDelegate {

    override fun type(): ActionType {
        return ActionType.METHOD
    }


    override fun inject(bundle: Bundle, target: Any) {
    }
    suspend fun factory(): Factory


    interface Factory {
        suspend fun create(
            contextHolder: ContextHolder,
            clazz: Class<*>,
            bundle: Bundle
        ): MethodInvoker
    }

}