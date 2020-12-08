package com.x930073498.router.impl

import android.app.Application
import android.os.Bundle
import com.x930073498.router.action.ActionCenter
import com.zx.common.auto.IApplicationLifecycle

abstract class AutoAction:ActionDelegate ,IApplicationLifecycle{
    override fun onApplicationCreated(app: Application) {
        ActionCenter.register(this)
    }
    protected fun injectParent(path:String,bundle: Bundle,target:Any){
        val action = ActionCenter.getAction(path)
        action.inject(bundle, target)
    }
}