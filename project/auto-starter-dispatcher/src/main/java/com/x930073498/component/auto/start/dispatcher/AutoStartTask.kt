package com.x930073498.component.auto.start.dispatcher

import com.x930073498.component.auto.IModuleRegister
import com.x930073498.component.starter.task.AppStartTask

abstract class AutoStartTask : AppStartTask(), IModuleRegister {
    final override fun register() {
        AutoStarterDispatcher.register(this)
    }
}