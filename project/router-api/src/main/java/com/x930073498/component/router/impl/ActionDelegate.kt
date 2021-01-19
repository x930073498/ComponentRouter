@file:Suppress("SafeCastWithReturn")

package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.auto.IAuto
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.thread.IThread


sealed class ActionType {
    object ACTIVITY : ActionType()
    object FRAGMENT : ActionType()
    object METHOD : ActionType()
    object SERVICE : ActionType()
    object INTERCEPTOR : ActionType()
    internal object SYSTEM : ActionType()
}

interface ActionDelegate : IAuto {
    fun type(): ActionType
    val path: String
    val thread: IThread
        get() {
            return IThread.ANY
        }

    val target: Target

    val autoRegister: Boolean get() = true

    fun inject(bundle: Bundle, target: Any)
    val group: String
        get() = ""

    fun interceptors(): List<String> = arrayListOf()


}


class SystemActionDelegate() : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.SYSTEM
    }



    private val _target = Target.SystemTarget()

    override val target: Target = _target

    override val path: String
        get() = ""

    override fun inject(bundle: Bundle, target: Any) {

    }
}






