package com.x930073498.kotlinpoet

import android.app.Application
import com.x930073498.kotlinpoet.test.TestModuleRegister
import com.x930073498.router.Router

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Router.init(this)
        TestModuleRegister.register()

    }
}