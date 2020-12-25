package com.x930073498.component.router.util

import android.app.Activity
import android.os.Bundle
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.registerActivityLifecycleCallbacks
import com.x930073498.component.core.unregisterActivityLifecycleCallbacks
import kotlinx.coroutines.*
import kotlin.coroutines.resume

internal suspend fun listenActivityCreated(key:String,value:String):Deferred<Activity?>{
   return coroutineScope {
       async {
           var lifecycle: IActivityLifecycle? = null
           val result = withTimeoutOrNull(1000 * 10) {
               suspendCancellableCoroutine<Activity> {
                   registerActivityLifecycleCallbacks(object : IActivityLifecycle {
                       init {
                           lifecycle = this
                       }

                       override fun onActivityCreated(
                           activity: Activity,
                           savedInstanceState: Bundle?
                       ) {
                           val intent = activity.intent
                           if (intent.getStringExtra(key) == value) {
                               it.resume(activity)
                           }
                       }
                   })
               }
           }
           lifecycle?.unregisterActivityLifecycleCallbacks()
           result
       }

    }


}