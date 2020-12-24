package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.router.action.ContextHolder

interface MethodActionDelegate : ActionDelegate {

    override fun type(): ActionType {
        return ActionType.METHOD
    }


    override fun inject(bundle: Bundle, target: Any) {
    }
     fun factory(): Factory


    interface Factory {
         fun create(
            contextHolder: ContextHolder,
            clazz: Class<*>,
            bundle: Bundle
        ): MethodInvoker
    }

}