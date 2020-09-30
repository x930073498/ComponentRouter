package com.x930073498.kotlinpoet.test

import androidx.annotation.Keep
import com.x930073498.annotations.ModuleRegisterAnnotation
import com.x930073498.router.impl.ModuleRegister
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.impl.Register

@Keep
object TestModuleRegister : ModuleRegister {
    override fun register() {
        ActionCenter.register(`_$$TestFragmentFragmentActionDelegateGenerated`())
        ActionCenter.register(com.x930073498.module1.`_$$TestFragmentFragmentActionDelegateGenerated`())
        ActionCenter.register(`_$$TestServiceImplServiceActionDelegateGenerated`())
    }


}