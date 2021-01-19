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
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.cast
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*

internal class SystemActionNavigatorImpl(
    private val listenable: ResultListenable<SystemNavigatorParams>
) : SystemActionNavigator {


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

    private val launchIntentLazy by lazy {
        listenable.map {
            with(it) {
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
                    return@with intent
                }
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(uri)
                intent.putExtras(bundle)
                return@with runCatching {
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
        }
    }

    override fun getLaunchIntent(): ResultListenable<Intent?> {
        return launchIntentLazy
    }

    override fun navigateForActivityResult(): ResultListenable<ActivityResult> {
        return getLaunchIntent().map {
            if (it == null) return@map ActivityResult(
                Activity.RESULT_CANCELED,
                null
            )
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

interface SystemActionNavigator : ActivityNavigator {


    companion object {
        internal fun create(
            listenable: ResultListenable<SystemNavigatorParams>,
        ): SystemActionNavigator {
            return SystemActionNavigatorImpl(listenable)
        }
    }
}