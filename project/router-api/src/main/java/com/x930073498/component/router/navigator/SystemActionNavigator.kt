package com.x930073498.component.router.navigator

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*

interface SystemActionNavigator : ActivityNavigator{


    companion object {
        internal fun create(
            target: Target.SystemTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): SystemActionNavigator {
            return object : SystemActionNavigator{

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
                    val uri = ParameterSupport.getUriAsString(bundle)
                    val context = contextHolder.getContext()
                    var intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
                    var info = context.packageManager.resolveActivity(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    if (info != null) {
                        if (info.activityInfo.packageName != context.packageName) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        return intent
                    }
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(uri)
                    intent.putExtras(bundle)
                    return runCatching {
                        info = context.packageManager.resolveActivity(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )
                        with(info) {
                            if (this == null) {
                                LogUtil.log(
                                    "没找到对应路径{'${
                                        ParameterSupport.getUriAsString(
                                            bundle
                                        )
                                    }'}的组件,请检查路径以及拦截器的设置"
                                )
                                null
                            } else {
                                if (activityInfo.packageName != context.packageName) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                intent
                            }
                        }
                    }.getOrNull()
                }

                override suspend fun navigateForActivityResult(): ActivityResult {
                    val intent=getLaunchIntent()?:return ActivityResult(Activity.RESULT_CANCELED,null)
                    return coroutineScope {
                        async {
                            val activity = listenerActivityCreated()
                            activityRef = WeakReference(activity)
                        }.start()
                        isInNavigation = true
                        val activity = contextHolder.getActivity()
                        launchAndWaitActivityResult(activity, activityMessenger, intent)
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

                override suspend fun navigate(): Any? {
                    requestActivity()
                    return null
                }

            }
        }
    }
}