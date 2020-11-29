package com.x930073498.router.impl

import android.app.Application
import com.x930073498.router.action.ActionCenter
import com.zx.common.auto.IApplicationLifecycle

abstract class AutoAction<T>:ActionDelegate<T> ,IApplicationLifecycle{
    override fun onApplicationCreated(app: Application) {
        ActionCenter.register(this)
    }
}