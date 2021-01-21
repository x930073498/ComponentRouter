package com.x930073498.component.router.util

import android.app.Activity
import android.os.Bundle
import com.x930073498.component.annotations.LaunchMode
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.currentActivity
import com.x930073498.component.core.registerActivityLifecycleCallbacks
import com.x930073498.component.core.unregisterActivityLifecycleCallbacks
import com.x930073498.component.router.coroutines.ResultSetter
import java.lang.ref.WeakReference

internal fun listenActivityCreated(
    key: String,
    value: String,
    className: String,
   launchMode: LaunchMode,
    setter: ResultSetter<Activity?>
) {
    var lifecycle: IActivityLifecycle? = null
    registerActivityLifecycleCallbacks(object : IActivityLifecycle {
        val currentActivityRef = WeakReference(currentActivity)

        init {
            lifecycle = this
        }

        private var hasResumed = false

        override fun onActivityPreCreated(
            activity: Activity,
            savedInstanceState: Bundle?
        ) {
            super.onActivityPreCreated(activity, savedInstanceState)
            setActivityCreate(activity)
        }

        private fun setActivityCreate(activity: Activity) {
            if (!hasResumed) {
                val intent = activity.intent
                if (intent.getStringExtra(key) == value || ((launchMode==LaunchMode.SingleTop||launchMode==LaunchMode.SingleTask) && currentActivityRef.get() === activity && activity.javaClass.name == className) || (currentActivityRef.get() !== activity && activity.javaClass.canonicalName == className)) {
                    setter.setResult(activity)
                    hasResumed = true
                    lifecycle?.unregisterActivityLifecycleCallbacks()
                }
            }
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?
        ) {
            setActivityCreate(activity)
        }

        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            super.onActivityPostCreated(activity, savedInstanceState)
            setActivityCreate(activity)
        }

        override fun onActivityPreStarted(activity: Activity) {
            super.onActivityPreStarted(activity)
            setActivityCreate(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            super.onActivityStarted(activity)
            setActivityCreate(activity)
        }

        override fun onActivityPostStarted(activity: Activity) {
            super.onActivityPostStarted(activity)
            setActivityCreate(activity)
        }

        override fun onActivityPreResumed(activity: Activity) {
            super.onActivityPreResumed(activity)
            setActivityCreate(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            setActivityCreate(activity)
        }

        override fun onActivityPostResumed(activity: Activity) {
            super.onActivityPostResumed(activity)
            setActivityCreate(activity)
        }
    })

}

