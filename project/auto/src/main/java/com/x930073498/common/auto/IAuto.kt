package com.x930073498.common.auto

import android.app.Application

interface IAuto

interface IRegister {
    fun register()
}

val IAuto.app: Application
    get() {
        return AutoTaskRegister.app
    }