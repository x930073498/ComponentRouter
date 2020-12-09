package com.x930073498.common.auto

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

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
            if (activity is FragmentActivity) {
                list.forEach {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(it, true)
                }
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            super.onActivityCreated(activity, savedInstanceState)
            activities.add(activity)
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
        this.app = getApplication(context)
        AutoActivityLifecycle.doRegister()
        load()
        ApplicationLifecycleHandler.onTaskComponentLoaded()
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
        if (task is IApplicationLifecycle) {
            task.doRegister()
        }
        if (task is IActivityLifecycle) {
            task.doRegister()
        }
        if (task is IFragmentLifecycle) {
            task.doRegister()
        }
        if (task is IInstanceActivityLifecycle<*>) {
            task.doRegister()
        }
        if (task is IRegister) {
            task.register()
        }

    }

}

