package com.x930073498.component.router.navigator

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.*
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



interface ActivityNavigator : ParameterProvider, Navigator {

    companion object {


        internal fun create(
            target: Target.ActivityTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): ActivityNavigator {
            return object : ActivityNavigator {

                private val activityMessenger = UUID.randomUUID().toString()
                private var hasNavigated = false
                private var isInNavigation = false


                private var activityRef = WeakReference<Activity>(null)
                private suspend fun listenerActivityCreated(): Activity? {
                    val result = listenActivityCreated(activityMessenger, activityMessenger).await()
                    hasNavigated = true
                    isInNavigation = false
                    return result
                }

                override fun getLaunchIntent(): Intent? {
                    val context = contextHolder.getContext()
                    val intent = Intent(context, target.targetClazz)
                    if (context is Application) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    bundle.putString(activityMessenger, activityMessenger)
                    intent.putExtras(bundle)
                    return intent
                }

                override suspend fun navigateForActivityResult(): ActivityResult {
                    return coroutineScope {
                        async {
                            val activity = listenerActivityCreated()
                            activityRef = WeakReference(activity)
                        }.start()
                        isInNavigation = true
                        val activity = contextHolder.getActivity()
                        launchAndWaitActivityResult(activity, activityMessenger, getLaunchIntent())
                    }
                }

                override suspend fun requestActivity(): Activity {
                    if (hasNavigated) return activityRef.get()!!
                    isInNavigation = true
                    return coroutineScope {
                        val job = async {
                            val activity = listenerActivityCreated()
                            activityRef = WeakReference(activity)
                            activity
                        }
                        contextHolder.getContext().startActivity(getLaunchIntent())
                        job.await()!!
                    }
                }

                override suspend fun <T : Activity> requestInstanceActivity(clazz: Class<T>): T {
                    return runCatching { requestActivity() as T }.getOrElse {
                        throw  RuntimeException("目标activity 不是 $clazz")
                    }
                }

                override fun getBundle(): Bundle {
                    return bundle
                }

                override fun getContextHolder(): ContextHolder {
                    return contextHolder
                }

                override suspend fun navigate(
                ): Any? {
                    requestActivity()
                    return null
                }

            }
        }
    }

    fun getLaunchIntent(): Intent?

    suspend fun navigateForActivityResult(): ActivityResult


    suspend fun requestActivity(): Activity

    suspend fun <T : Activity> requestInstanceActivity(clazz: Class<T>): T

}