package com.zx.common.auto

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

internal object AutoTaskRegister {
    internal lateinit var app: Application
    private var hasInit = false
    internal val activityLifecycle = FragmentAutoActivityLifecycle()

    @JvmStatic
    internal fun init(app: Application) {
        if (hasInit) return
        this.app = app
        activityLifecycle.register()
        load()
    }

    @JvmStatic
    private fun load() {
    }

    @JvmStatic
    private fun register(task: IAuto) {
        if (task is IApplicationLifecycle) {
            task.onApplicationCreated(app)
        }
        if (task is IActivityLifecycle) {
            task.register()
        }
        if (task is IFragmentLifecycle) {
            task.register()
        }

    }
}

internal class FragmentAutoActivityLifecycle() : IActivityLifecycle {
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
}