package com.x930073498.auto.start.dispatcher

import android.app.Application
import com.x930073498.common.auto.IApplicationLifecycle
import com.x930073498.common.auto.IAuto
import com.x930073498.starter.dispatcher.AppStartTaskDispatcher

internal class AutoStarterDispatcher : IAuto, IApplicationLifecycle {
    companion object {
        private val dispatcher = AppStartTaskDispatcher.getInstance()
        internal fun register(task: AutoStartTask) {
            dispatcher.addAppStartTask(task)
        }
    }

    override fun onTaskComponentLoaded(app: Application) {
        dispatcher.setContext(app)
        dispatcher.start()
        dispatcher.await()
    }
}


