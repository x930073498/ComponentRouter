package com.x930073498.component.router.navigator.impl

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResult
import com.x930073498.component.annotations.LaunchMode
import com.x930073498.component.annotations.LaunchMode.*
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.*
import com.x930073498.component.router.navigator.ActivityNavigator
import com.x930073498.component.router.navigator.ActivityNavigatorParams
import com.x930073498.component.router.navigator.NavigatorOption
import com.x930073498.component.router.navigator.NavigatorResult
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import java.lang.ref.WeakReference
import java.util.*

internal open class ActivityNavigatorImpl constructor(
    private val listenable: ResultListenable<ActivityNavigatorParams>,
    private val activityNavigatorOption: NavigatorOption.ActivityNavigatorOption,
) : ActivityNavigator {
    private val activityMessenger = UUID.randomUUID().toString()


    private var activityRef = WeakReference<Activity>(null)


    private var launchMode = activityNavigatorOption.launchMode ?: Standard

    open fun createIntent(): ResultListenable<Intent?> {
        return listenable.map {

            when (val target = it.target) {
                is Target.ActivityTarget -> {
                    it.run {
                        bundle.putString(activityMessenger, activityMessenger)
                        val context = contextHolder.getContext()
                        val action = target.action
                        launchMode = activityNavigatorOption.launchMode ?: action.launchMode()
                        val intent = Intent(context, target.targetClazz)
                        val componentName =
                            intent.resolveActivity(contextHolder.getPackageManager())
                        if (componentName == null) {
                            null
                        } else {
                            intent.apply {
                                if (context is Application) {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                } else {
                                    when (launchMode) {
                                        Standard -> {
                                            //doNothing
                                            LogUtil.log("enter this line Standard")
                                        }
                                        SingleTop -> {
                                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        }
                                        SingleTask -> {
                                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                        NewTask -> {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    }
                                }

                                putExtras(bundle)
                            }
                        }
                    }
                }
                else -> {
                    with(it) {
                        bundle.putString(activityMessenger, activityMessenger)
                        val uri = ParameterSupport.getUriAsString(bundle)
                        val context = contextHolder.getContext()
                        var intent = Intent.parseUri(uri, 0)
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
                                            uri
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


        }
    }

    private val launchIntentLazy by lazy {
        createIntent()
    }

    private val createRequestActivityListenable by lazy {
        launchIntentLazy.setter {
            if (it == null) {
                setResult(null)
                return@setter
            }
            val params = listenable.await()
            val componentName = it.resolveActivity(params.contextHolder.getPackageManager())
            componentName.listenerActivityCreated(launchMode, this)
            params.contextHolder.getContext().startActivity(it)
        }.listen {
            activityRef = WeakReference(it)
        }
    }

    private val requestActivityLazy: ResultListenable<Activity?>
        get() {
            return createRequestActivityListenable
        }

    private fun ComponentName.listenerActivityCreated(
        launchMode: LaunchMode,
        listenable: ResultSetter<Activity?>
    ) {
        listenActivityCreated(
            activityMessenger,
            activityMessenger,
            this.className,
            launchMode,
            listenable
        )
    }


    override fun getLaunchIntent(): ResultListenable<Intent?> {
        return launchIntentLazy
    }

    override fun navigateForActivityResult(activity: Activity): ResultListenable<ActivityResult> {
        val anchorActivityRef = WeakReference(activity)
        launchIntentLazy.setter {
            if (it == null) {
                setResult(null)
                return@setter
            }
            val params = listenable.await()
            val componentName = it.resolveActivity(params.contextHolder.getPackageManager())
            if (componentName == null) {
                setResult(null)
                return@setter
            }
            componentName.listenerActivityCreated(launchMode, this)
        }.listen {
            activityRef = WeakReference(it)
        }
        return launchIntentLazy.setter {
            val anchorActivity = anchorActivityRef.get()
            if (anchorActivity == null) {
                setResult(
                    ActivityResult(Activity.RESULT_CANCELED, null)
                )
            } else
                launchAndWaitActivityResult(anchorActivity, activityMessenger, it, this)
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