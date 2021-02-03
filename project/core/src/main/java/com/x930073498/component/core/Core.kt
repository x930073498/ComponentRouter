package com.x930073498.component.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.core.app.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.x930073498.component.auto.IAuto


val currentActivity: Activity
    get() {
        return AutoTaskRegister.AutoActivityLifecycle.getTopActivity()
    }
val app by lazy { AutoTaskRegister.app }

fun Activity.requireActivity(): Activity = this
fun Activity.requireContext(): Context = requireActivity()

fun ComponentActivity.requireLifecycleOwner(): LifecycleOwner = this
fun ComponentActivity.requireLifecycle(): Lifecycle = requireLifecycleOwner().lifecycle

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
