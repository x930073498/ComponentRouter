package com.x930073498.common.auto

import android.app.Application

interface IApplicationLifecycle {
    fun onApplicationCreated(app: Application) {

    }

    fun onTaskComponentLoaded(app: Application) {

    }

    fun onApplicationExited(app: Application) {

    }

    fun onApplicationBringToFront(app: Application) {

    }

    fun onApplicationSwitchToBackground(app: Application) {

    }
}

internal fun IApplicationLifecycle.doRegister() {
    ApplicationLifecycleHandler.register(this)
}

internal object ApplicationLifecycleHandler {
    private val app = AutoTaskRegister.app
    private val lifecycles = arrayListOf<IApplicationLifecycle>()

    fun register(lifecycle: IApplicationLifecycle) {
        lifecycle.onApplicationCreated(app)
        lifecycles.add(lifecycle)
    }

    fun onTaskComponentLoaded() {
        lifecycles.forEach {
            it.onTaskComponentLoaded(app)
        }
    }

    fun onApplicationExit() {
        lifecycles.forEach {
            it.onApplicationExited(app)
        }
    }

    fun onApplicationBringToFront() {
        lifecycles.forEach {
            it.onApplicationBringToFront(app)
        }
    }

    fun onApplicationSwitchToBackground() {
        lifecycles.forEach {
            it.onApplicationSwitchToBackground(app)
        }
    }


}




