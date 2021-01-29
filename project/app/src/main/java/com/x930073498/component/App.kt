package com.x930073498.component

import androidx.multidex.MultiDexApplication
import com.x930073498.component.auto.*
import com.x930073498.component.auto.annotations.AutoClass
import com.x930073498.component.router.byRouter
import com.x930073498.component.test.Data

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        LogUtil.log(Data("测试data"))

    }

}

class AutoConfig : IConfiguration, IAuto {
    override fun option(holder: ConfigurationHolder) {
        holder.byDefault {
            if (BuildConfig.DEBUG) {
                setDebug(true)
            }
        }
        holder.byRouter {
            activityPropertyAutoInject(true)
            fragmentPropertyAutoInject(true)
        }
    }


}

@AutoClass("annotation")
class A {
    init {
    }
}

//@AutoClass("annotation")
class B {
    init {
        println("enter this line 9898")
    }
}
