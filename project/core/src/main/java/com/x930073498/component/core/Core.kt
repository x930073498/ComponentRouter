package com.x930073498.component.core

import android.os.Looper


val currentActivity = AutoTaskRegister.AutoActivityLifecycle.getTopActivity()
val app by lazy { AutoTaskRegister.app }

val isMainThread=Looper.getMainLooper()== Looper.myLooper()

fun registerActivityLifecycleCallbacks(activityLifecycle: IActivityLifecycle){
    app.registerActivityLifecycleCallbacks(ActivityLifecycle.get(activityLifecycle))
}
fun unregisterActivityLifecycleCallbacks(activityLifecycle: IActivityLifecycle){
    activityLifecycle.unregisterActivityLifecycleCallbacks()
}