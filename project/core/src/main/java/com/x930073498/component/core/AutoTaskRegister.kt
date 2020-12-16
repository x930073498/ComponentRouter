package com.x930073498.component.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.x930073498.component.auto.*

internal object AutoTaskRegister {
    internal object AutoActivityLifecycle : IActivityLifecycle {
        private val activities = arrayListOf<Activity>()
        private var startActivityCount = 0
        private val list = arrayListOf<FragmentManager.FragmentLifecycleCallbacks>()
        fun add(lifecycle: FragmentManager.FragmentLifecycleCallbacks) {
            if (!list.contains(lifecycle)) {
                list.add(lifecycle)
            }
        }

        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
            if (activity is FragmentActivity) {
                list.forEach {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(it, true)
                }
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            super.onActivityCreated(activity, savedInstanceState)

        }

        private var isInFront = false
        override fun onActivityStarted(activity: Activity) {
            super.onActivityStarted(activity)
            startActivityCount++
            if (startActivityCount == 1) {
                isInFront = true
            }

        }

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            if (isInFront) {
                isInFront = false
                ApplicationLifecycleHandler.onApplicationBringToFront()
            }
        }


        override fun onActivityStopped(activity: Activity) {
            super.onActivityStopped(activity)
            startActivityCount--
            if (startActivityCount == 0) {
                ApplicationLifecycleHandler.onApplicationSwitchToBackground()
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            super.onActivityDestroyed(activity)
            activities.remove(activity)
            if (activities.isEmpty()) {
                ApplicationLifecycleHandler.onApplicationExit()
            }
        }
    }

    internal lateinit var app: Application
    private var hasInit = false


    @JvmStatic
    internal fun init(context: Context) {
        if (hasInit) return
        val time = System.currentTimeMillis()
        app = getApplication(context)
        load()
        applyConfiguration {
            LogUtil.setLogger { tag, msg ->
                Log.i(tag, getSerializer()?.serialize(msg) ?: msg.toString())
            }
        }   //初始化配置
        ModuleHandler.doRegister()//初始化模块
        AutoActivityLifecycle.doRegister()//注册自身activity生命周期监听
        ApplicationLifecycleHandler.onApplicationCreated()//通知application create
        RegisterHandler.doRegister()//注册器进行注册操作
        LogUtil.log("初始化耗时，${System.currentTimeMillis() - time}ms")
    }

    private fun getApplication(context: Context): Application {
        if (context is Application) return context
        val appContext = context.applicationContext
        return appContext as Application
    }


    @JvmStatic
    private fun load() {

    }

    @JvmStatic
    private fun register(task: IAuto) {
        if (task is IConfiguration) {
            task.register()
        }
        if (task is IApplicationLifecycle) {
            task.doRegister()
        }
        if (task is IFragmentLifecycle) {
            task.doRegister()
        }
        if (task is IInstanceActivityLifecycle<*>) {
            task.doRegister()
        } else if (task is IActivityLifecycle) {
            task.doRegister()
        }
        if (task is IModuleRegister) {
            task.doRegister()
        } else if (task is IRegister) {
            task.doRegister()
        }

    }

}
