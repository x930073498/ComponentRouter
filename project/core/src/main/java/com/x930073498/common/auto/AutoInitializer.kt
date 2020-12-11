package com.x930073498.common.auto

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class AutoInitializer: Initializer<Unit> {
    override fun create(context: Context) {
     AutoTaskRegister.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return arrayListOf()
    }
}
val IAuto.app: Application
    get() {
        return AutoTaskRegister.app
    }