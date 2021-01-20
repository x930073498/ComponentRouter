package com.x930073498.component.router.util

import android.app.Activity
import android.os.Bundle
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.registerActivityLifecycleCallbacks
import com.x930073498.component.core.unregisterActivityLifecycleCallbacks
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.ResultSetter
import kotlinx.coroutines.*
import kotlin.coroutines.resume

internal fun listenActivityCreated(
    key: String,
    value: String,
    setter: ResultSetter<Activity?>
) {
    var lifecycle: IActivityLifecycle? = null

    registerActivityLifecycleCallbacks(object : IActivityLifecycle {
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
                if (intent.getStringExtra(key) == value) {
                    setter.setResult(activity)
                    hasResumed = true
                }
                lifecycle?.unregisterActivityLifecycleCallbacks()
            }
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?
        ) {
            setActivityCreate(activity)
        }
    })

}

