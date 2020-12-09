package com.x930073498.auto.start.dispatcher

import com.x930073498.common.auto.IRegister
import com.x930073498.starter.task.AppStartTask

abstract class AutoStartTask : AppStartTask(), IRegister {
    final override fun register() {
        AutoStarterDispatcher.register(this)
    }
}