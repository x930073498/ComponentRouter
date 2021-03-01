package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.annotations.InterceptorScope
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target


interface InterceptorActionDelegate : ActionDelegate, Comparable<InterceptorActionDelegate> {
    override fun type(): ActionType {
        return ActionType.INTERCEPTOR
    }

    override val target: Target.InterceptorTarget

    val priority: Int
        get() = 0

    fun factory(): Factory
    val scope: InterceptorScope
        get() = InterceptorScope.NORMAL

    override fun inject(bundle: Bundle, target: Any) {

    }


    override fun compareTo(other: InterceptorActionDelegate): Int {
        val priorityResult = priority.compareTo(other.priority)
        if (priorityResult != 0) return priorityResult
        return scope.compareTo(other.scope)
    }

    interface Factory {
        fun create(contextHolder: ContextHolder, clazz: Class<*>): RouterInterceptor
    }
}