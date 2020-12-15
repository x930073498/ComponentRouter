package com.x930073498.component.core

import android.app.Application
import com.x930073498.component.auto.IAuto

interface IApplicationLifecycle{
    fun onApplicationCreated(app: Application) {

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
        lifecycles.add(lifecycle)
    }
    fun onApplicationCreated(){
        lifecycles.forEach {
            it.onApplicationCreated(app)
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




