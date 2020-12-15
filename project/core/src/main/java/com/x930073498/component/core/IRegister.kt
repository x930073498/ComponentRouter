package com.x930073498.component.core

import com.x930073498.component.auto.IRegister


interface IModuleRegister:IRegister
internal object ModuleHandler{
    private val list= arrayListOf<IModuleRegister>()

    fun add(register:IModuleRegister){
        list.add(register)
    }
    fun doRegister(){
        list.forEach {
            it.register()
        }
    }

}

internal fun IRegister.doRegister(){
    RegisterHandler.add(this)
}
internal fun IModuleRegister.doRegister(){
    ModuleHandler.add(this)
}

internal object RegisterHandler{
    private val list= arrayListOf<IRegister>()

    fun add(register:IRegister){
        list.add(register)
    }
    fun doRegister(){
        list.forEach {
            it.register()
        }
    }

}