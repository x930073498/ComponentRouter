package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.annotations.InterceptorScope
import com.x930073498.component.auto.IRegister
import com.x930073498.component.router.Router
import com.x930073498.component.router.action.ActionCenter

abstract class AutoAction : ActionDelegate, IRegister {
    override fun register() {
        if (this is InterceptorActionDelegate) {
            doRegister()
        } else
            ActionCenter.register(this)
    }


    protected fun injectParent(path: String, bundle: Bundle, target: Any) {
        val action = ActionCenter.getAction(path)
        action.inject(bundle, target)
    }
}


internal fun InterceptorActionDelegate.doRegister() {
    when (scope) {
        InterceptorScope.NORMAL -> {
            ActionCenter.register(this)
        }
        InterceptorScope.GLOBAL -> {
            Router.addGlobalInterceptor(this)
        }
        InterceptorScope.ALL -> {
            ActionCenter.register(this)
            Router.addGlobalInterceptor(this)
        }
    }
}
