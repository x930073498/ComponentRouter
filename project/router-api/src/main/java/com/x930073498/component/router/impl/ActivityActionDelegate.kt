package com.x930073498.component.router.impl

interface ActivityActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.ACTIVITY
    }
}