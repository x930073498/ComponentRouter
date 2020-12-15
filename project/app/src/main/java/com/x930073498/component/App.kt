package com.x930073498.component

import android.app.Application
import com.x930073498.component.auto.IAuto
import com.x930073498.component.core.AutoConfiguration
import com.x930073498.component.core.IConfiguration
import com.x930073498.component.core.LogUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var foo: Foo
    override fun onCreate() {
        super.onCreate()
        foo.test()

    }

    fun a() {
        LogUtil.log("enter this line App")
    }
}

class AutoConfig : IConfiguration(), IAuto {
    override fun AutoConfiguration.config() {
        if (BuildConfig.DEBUG) {
            checkRouteUnique()
            debug()
        }
    }
}