package com.x930073498.component.auto.start.dispatcher

import android.app.Application
import com.x930073498.component.core.IApplicationLifecycle
import com.x930073498.component.auto.IAuto
import com.x930073498.component.starter.dispatcher.AppStartTaskDispatcher

internal class AutoStarterDispatcher : IAuto, IApplicationLifecycle {
    companion object {
        private val dispatcher = AppStartTaskDispatcher.getInstance()
        internal fun register(task: AutoStartTask) {
            dispatcher.addAppStartTask(task)
        }
    }

    override fun onApplicationCreated(app: Application) {
        super.onApplicationCreated(app)
        dispatcher.setContext(app)
        dispatcher.start()
        dispatcher.await()
    }

}


