@file:Suppress("SafeCastWithReturn")

package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target
import com.x930073498.router.util.ParameterSupport


sealed class ActionType {
    object ACTIVITY : ActionType()
    object FRAGMENT : ActionType()
    object METHOD : ActionType()
    object SERVICE : ActionType()
    object INTERCEPTOR : ActionType()
    internal object SYSTEM : ActionType()
}

interface ActionDelegate {
    fun type(): ActionType
    val path: String


    val target:Target

    fun inject(bundle: Bundle, target: Any)
    val group: String
        get() = ""

    fun interceptors(): List<String> = arrayListOf()




}


internal object SystemActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.SYSTEM
    }

    override val target: Target
        get() = Target.SystemTarget

    override val path: String
        get() = ""

    override fun inject(bundle: Bundle, target: Any) {

    }


}


suspend fun ActionDelegate.navigate(bundle: Bundle, contextHolder: ContextHolder): Any? {
    ParameterSupport.putCenter(bundle, path)
    return target.go(bundle, contextHolder)
}


