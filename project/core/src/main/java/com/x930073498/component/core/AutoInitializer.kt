package com.x930073498.component.core

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.x930073498.component.auto.IAuto

class AutoInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        AutoTaskRegister.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return arrayListOf()
    }
}
