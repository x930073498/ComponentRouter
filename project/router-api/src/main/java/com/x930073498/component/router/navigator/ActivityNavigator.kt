package com.x930073498.component.router.navigator

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.AwaitResult
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.cast
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import kotlinx.coroutines.*
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*

internal open class ActivityNavigatorImpl internal constructor(
    private val listenable: ResultListenable<ActivityNavigatorParams>,
) : ActivityNavigator {
    private val activityMessenger = UUID.randomUUID().toString()
    private var hasNavigated = false
    private var isInNavigation = false


    private var activityRef = WeakReference<Activity>(null)

    private val launchIntentLazy by lazy {
        listenable.map<ActivityNavigatorParams, Intent?> {
            it.run {
                val context = contextHolder.getContext()
                Intent(context, target.targetClazz).apply {
                    if (context is Application) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    bundle.putString(activityMessenger, activityMessenger)
                    putExtras(bundle)
                }
            }
        }
    }

    private val createRequestActivityListenable by lazy {
        launchIntentLazy.map {
            if (it == null) {
                return@map null
            }
            coroutineScope {
                val job = async {
                    val activity = listenerActivityCreated()
                    activityRef = WeakReference(activity)
                    activity
                }
                listenable.await().contextHolder.getContext().startActivity(it)
                job.await()
            }
        }
    }

    private val requestActivityLazy: ResultListenable<Activity?>
        get() {
            if (hasNavigated) return listenable.map { activityRef.get() }
            isInNavigation = true
            return createRequestActivityListenable
        }

    private suspend fun listenerActivityCreated(): Activity? {
        val result = listenActivityCreated(activityMessenger, activityMessenger).await()
        hasNavigated = true
        isInNavigation = false
        return result
    }


    override fun getLaunchIntent(): ResultListenable<Intent?> {
        return launchIntentLazy
    }

    override fun navigateForActivityResult(): ResultListenable<ActivityResult> {
        return launchIntentLazy.map {
            coroutineScope {
                async {
                    val activity = listenerActivityCreated()
                    activityRef = WeakReference(activity)
                }.start()
                isInNavigation = true
                val activity = listenable.await().contextHolder.getActivity()
                launchAndWaitActivityResult(activity, activityMessenger, it)
            }
        }
    }

    override fun requestActivity(): ResultListenable<Activity?> {
        return requestActivityLazy
    }

    override fun <T : Activity> requestInstanceActivity(clazz: Class<T>): ResultListenable<T> {
        return requestActivity().cast()
    }

    override fun navigate(): ResultListenable<NavigatorResult> {
        return requestActivity().map {
            NavigatorResult.ACTIVITY(it)
        }
    }

}


interface ActivityNavigator : Navigator {

    companion object {
        internal fun create(
            listenable: ResultListenable<ActivityNavigatorParams>,
        ): ActivityNavigator {
            return ActivityNavigatorImpl(listenable)
        }
    }

    fun getLaunchIntent(): ResultListenable<Intent?>

    fun navigateForActivityResult(): ResultListenable<ActivityResult>


    fun requestActivity(): ResultListenable<Activity?>

    fun <T : Activity> requestInstanceActivity(clazz: Class<T>): ResultListenable<T>

}

