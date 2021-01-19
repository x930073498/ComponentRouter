package com.x930073498.component.core

import android.app.Activity
import android.os.Looper


val currentActivity: Activity
    get() {
        return AutoTaskRegister.AutoActivityLifecycle.getTopActivity()
    }
val app by lazy { AutoTaskRegister.app }

val isMainThread: Boolean
    get() {
        return Looper.getMainLooper() == Looper.myLooper()
    }

fun registerActivityLifecycleCallbacks(activityLifecycle: IActivityLifecycle) {
    app.registerActivityLifecycleCallbacks(ActivityLifecycle.get(activityLifecycle))
}

fun unregisterActivityLifecycleCallbacks(activityLifecycle: IActivityLifecycle) {
    activityLifecycle.unregisterActivityLifecycleCallbacks()
}