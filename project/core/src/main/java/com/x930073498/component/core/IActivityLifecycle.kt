package com.x930073498.component.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.x930073498.component.auto.IAuto

interface IActivityLifecycle {
    fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    fun onActivityPreStarted(activity: Activity) {
    }

    fun onActivityStarted(activity: Activity) {
    }

    fun onActivityPostStarted(activity: Activity) {
    }

    fun onActivityPreResumed(activity: Activity) {
    }

    fun onActivityResumed(activity: Activity) {
    }

    fun onActivityPostResumed(activity: Activity) {
    }

    fun onActivityPrePaused(activity: Activity) {
    }

    fun onActivityPaused(activity: Activity) {
    }

    fun onActivityPostPaused(activity: Activity) {
    }

    fun onActivityPreStopped(activity: Activity) {
    }

    fun onActivityStopped(activity: Activity) {
    }

    fun onActivityPostStopped(activity: Activity) {
    }

    fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
    }

    fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
    }

    fun onActivityPreDestroyed(activity: Activity) {
    }

    fun onActivityDestroyed(activity: Activity) {
    }

    fun onActivityPostDestroyed(activity: Activity) {
    }
}

fun IActivityLifecycle.unregisterActivityLifecycleCallbacks() {
    app.unregisterActivityLifecycleCallbacks(ActivityLifecycle.get(this))
}

internal fun IActivityLifecycle.doRegister() {
    ActivityLifecycle.get(this).register()
}

internal class ActivityLifecycle(private val lifecycle: IActivityLifecycle) :
    Application.ActivityLifecycleCallbacks {
    val app: Application
    private var hasRegister = false

    init {
        map[lifecycle] = this
        app = AutoTaskRegister.app
    }

    fun register() {
        if (hasRegister) return
        app.registerActivityLifecycleCallbacks(this)
        hasRegister = true
    }

    companion object {
        private val map = mutableMapOf<IActivityLifecycle, ActivityLifecycle>()

        fun get(lifecycle: IActivityLifecycle): ActivityLifecycle {
            return map[lifecycle] ?: ActivityLifecycle(lifecycle)
        }
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        lifecycle.onActivityPreCreated(activity, savedInstanceState)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        lifecycle.onActivityCreated(activity, savedInstanceState)
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        lifecycle.onActivityPostCreated(activity, savedInstanceState)
    }

    override fun onActivityPreStarted(activity: Activity) {
        lifecycle.onActivityPreStarted(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        lifecycle.onActivityStarted(activity)
    }

    override fun onActivityPostStarted(activity: Activity) {
        lifecycle.onActivityPostStarted(activity)
    }

    override fun onActivityPreResumed(activity: Activity) {
        lifecycle.onActivityPreResumed(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        lifecycle.onActivityResumed(activity)
    }

    override fun onActivityPostResumed(activity: Activity) {
        lifecycle.onActivityPostResumed(activity)
    }

    override fun onActivityPrePaused(activity: Activity) {
        lifecycle.onActivityPrePaused(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        lifecycle.onActivityPaused(activity)
    }

    override fun onActivityPostPaused(activity: Activity) {
        lifecycle.onActivityPostPaused(activity)
    }

    override fun onActivityPreStopped(activity: Activity) {
        lifecycle.onActivityPreStopped(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        lifecycle.onActivityStopped(activity)
    }

    override fun onActivityPostStopped(activity: Activity) {
        lifecycle.onActivityPostStopped(activity)
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
        lifecycle.onActivityPreSaveInstanceState(activity, outState)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        lifecycle.onActivitySaveInstanceState(activity, outState)
    }

    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
        lifecycle.onActivityPostSaveInstanceState(activity, outState)
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        lifecycle.onActivityPreDestroyed(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        lifecycle.onActivityDestroyed(activity)
    }

    override fun onActivityPostDestroyed(activity: Activity) {
        lifecycle.onActivityPostDestroyed(activity)
    }
}

