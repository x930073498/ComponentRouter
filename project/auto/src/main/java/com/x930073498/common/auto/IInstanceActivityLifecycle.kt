@file:Suppress("UNCHECKED_CAST")

package com.x930073498.common.auto

import android.app.Activity
import android.os.Bundle

interface IInstanceActivityLifecycle<T> where T : Activity {

     fun getTargetClass(): Class<*>

    fun onActivityPreCreated(activity: T, savedInstanceState: Bundle?) {
    }

    fun onActivityCreated(activity: T, savedInstanceState: Bundle?) {
    }

    fun onActivityPostCreated(activity: T, savedInstanceState: Bundle?) {
    }

    fun onActivityPreStarted(activity: T) {
    }

    fun onActivityStarted(activity: T) {
    }

    fun onActivityPostStarted(activity: T) {
    }

    fun onActivityPreResumed(activity: T) {
    }

    fun onActivityResumed(activity: T) {
    }

    fun onActivityPostResumed(activity: T) {
    }

    fun onActivityPrePaused(activity: T) {
    }

    fun onActivityPaused(activity: T) {
    }

    fun onActivityPostPaused(activity: T) {
    }

    fun onActivityPreStopped(activity: T) {
    }

    fun onActivityStopped(activity: T) {
    }

    fun onActivityPostStopped(activity: T) {
    }

    fun onActivityPreSaveInstanceState(activity: T, outState: Bundle) {
    }

    fun onActivitySaveInstanceState(activity: T, outState: Bundle) {
    }

    fun onActivityPostSaveInstanceState(activity: T, outState: Bundle) {
    }

    fun onActivityPreDestroyed(activity: T) {
    }

    fun onActivityDestroyed(activity: T) {
    }

    fun onActivityPostDestroyed(activity: T) {
    }
}

internal fun IInstanceActivityLifecycle<*>.doRegister() {
    asActivityLifecycle().doRegister()
}

private fun <T : Activity> IInstanceActivityLifecycle<T>.asActivityLifecycle(): IActivityLifecycle {
    return InstanceActivityLifecycle(this)
}

private class InstanceActivityLifecycle<T>(
    private val lifecycle: IInstanceActivityLifecycle<T>
) : IActivityLifecycle where T : Activity {
    private val target = lifecycle.getTargetClass()
    private fun check(activity: Activity, action: IInstanceActivityLifecycle<T>.(T) -> Unit) {
        if (target.isInstance(activity)) {
            activity as T
            action(lifecycle, activity)
        }
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        check(activity) {
            onActivityPreCreated(it, savedInstanceState)
        }

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        check(activity) {
            onActivityCreated(it, savedInstanceState)
        }
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        check(activity) {
            onActivityPostCreated(it, savedInstanceState)
        }
    }

    override fun onActivityPreStarted(activity: Activity) {
        check(activity) {
            onActivityPreStarted(it)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        check(activity) {
            onActivityStarted(it)
        }
    }

    override fun onActivityPostStarted(activity: Activity) {
        check(activity) {
            onActivityPostStarted(it)
        }
    }

    override fun onActivityPreResumed(activity: Activity) {
        check(activity) { onActivityPreResumed(it) }
    }

    override fun onActivityResumed(activity: Activity) {
        check(activity) { onActivityResumed(it) }
    }

    override fun onActivityPostResumed(activity: Activity) {
        check(activity) { onActivityPostResumed(it) }
    }

    override fun onActivityPrePaused(activity: Activity) {
        check(activity) { onActivityPrePaused(it) }
    }

    override fun onActivityPaused(activity: Activity) {
        check(activity) { onActivityPaused(it) }
    }

    override fun onActivityPostPaused(activity: Activity) {
        check(activity) {
            onActivityPostPaused(it)
        }
    }

    override fun onActivityPreStopped(activity: Activity) {
        check(activity) { onActivityPreStopped(it) }
    }

    override fun onActivityStopped(activity: Activity) {
        check(activity) { onActivityStopped(it) }
    }

    override fun onActivityPostStopped(activity: Activity) {
        check(activity) { onActivityPostStopped(it) }
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
        check(activity) {
            onActivityPreSaveInstanceState(it, outState)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        check(activity) {
            onActivitySaveInstanceState(it, outState)
        }
    }

    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
        check(activity) {
            onActivityPostSaveInstanceState(it, outState)
        }
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        check(activity) { onActivityPreDestroyed(it) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        check(activity) { onActivityDestroyed(it) }
    }

    override fun onActivityPostDestroyed(activity: Activity) {
        check(activity) { onActivityPostDestroyed(it) }
    }
}




