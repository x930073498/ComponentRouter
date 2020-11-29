package com.x930073498.kotlinpoet.test

import android.app.Application
import androidx.annotation.Keep
import com.x930073498.annotations.ModuleRegisterAnnotation
import com.x930073498.router.impl.ModuleRegister
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.impl.Register
import com.zx.common.auto.IApplicationLifecycle
import com.zx.common.auto.IAuto

@Keep
class TestModuleRegister : ModuleRegister{

    override fun register() {
        ActionCenter.register(`_$$TestFragmentFragmentActionDelegateGenerated`())
        ActionCenter.register(com.x930073498.module1.`_$$TestFragmentFragmentActionDelegateGenerated`())
        ActionCenter.register(`_$$TestServiceImplServiceActionDelegateGenerated`())
    }

//    override fun onApplicationCreated(app: Application) {
//        register()
//    }


}