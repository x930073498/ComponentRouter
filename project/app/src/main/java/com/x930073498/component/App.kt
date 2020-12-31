package com.x930073498.component

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.x930073498.component.auto.*
import com.x930073498.component.test.Data
import dagger.hilt.android.HiltAndroidApp
import java.lang.reflect.Type
import javax.inject.Inject

@HiltAndroidApp
class App : MultiDexApplication() {

    @Inject
    lateinit var foo: Foo
    override fun onCreate() {
        super.onCreate()
        foo.test()
        LogUtil.log(Data("测试data"))

    }

    fun a() {
        LogUtil.log("enter this line App")
    }
}

class AutoConfig : IConfiguration(), IAuto {
    override fun ConfigurationHandler.config() {
        if (BuildConfig.DEBUG) {
            checkRouteUnique()
            debug()
        }
    }

}
@AutoClass("autoRegister")
class A : IAuto {
    init {
        println("enter this line 99999")
    }
}
@AutoClass("autoRegister")
class B : IAuto {
    init {
        println("enter this line 9898")
    }
}
