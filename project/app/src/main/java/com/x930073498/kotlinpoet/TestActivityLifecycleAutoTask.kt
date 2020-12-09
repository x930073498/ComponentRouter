package com.x930073498.kotlinpoet

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.x930073498.common.auto.*

//模块化工具,只需要在任意模块中实现IAuto接口,无需做其他配置,实现的代码都可以在特定的是否自动运行,
// IActivityLifecycle绑定的是activity的生命周期,app内部任意activity执行的生命周期,都会在特定的方法中回调
//IApplicationLifecycle,主要用于application初始化,onApplicationCreated方法会在application启动时调用,在application.OnCreate()方法前调用
//IFragmentLifecycle,绑定的是fragment的生命周期,app内任意的fragment执行生命周期,都会在特定的方法中回调
class TestActivityLifecycleAutoTask : IAuto, IActivityLifecycle {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        println("enter this line 1401410")
    }
}

class TestApplicationStartAutoTask : IAuto, IApplicationLifecycle {
    override fun onApplicationCreated(app: Application) {
        println("enter this line 18794")
    }
}

class TestFragmentLifecycleAutoTask() : IAuto, IFragmentLifecycle {
    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        println("enter this line 1747,f=$f")
    }
}

class TestInstanceActivityLifecycleAutoTask : IAuto, IInstanceActivityLifecycle<MainActivity> {
    override fun onActivityCreated(activity: MainActivity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        println("enter this line TestInstanceActivityLifecycleAutoTask")
    }

    override fun getTargetClass(): Class<*> {
        return MainActivity::class.java
    }
}