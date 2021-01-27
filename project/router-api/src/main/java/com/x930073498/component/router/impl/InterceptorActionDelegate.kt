package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.annotations.InterceptorScope
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target

interface InterceptorActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.INTERCEPTOR
    }

    override val target: Target.InterceptorTarget
     fun factory(): Factory
    val scope: InterceptorScope
        get() = InterceptorScope.NORMAL

    override fun inject(bundle: Bundle, target: Any) {

    }


    interface Factory {
         fun create(contextHolder: ContextHolder, clazz: Class<*>): RouterInterceptor
    }
}