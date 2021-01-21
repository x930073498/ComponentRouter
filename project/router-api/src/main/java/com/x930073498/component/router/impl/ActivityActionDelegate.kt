package com.x930073498.component.router.impl

import com.x930073498.component.annotations.LaunchMode

interface ActivityActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.ACTIVITY
    }
    fun launchMode()=LaunchMode.Standard
}