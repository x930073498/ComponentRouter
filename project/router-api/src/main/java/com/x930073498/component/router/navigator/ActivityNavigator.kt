package com.x930073498.component.router.navigator

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResult
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.*
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.component.router.util.launchAndWaitActivityResult
import com.x930073498.component.router.util.listenActivityCreated
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.*

internal open class ActivityNavigatorImpl constructor(
    private val listenable: ResultListenable<ActivityNavigatorParams>,
    private val activityNavigatorOption: NavigatorOption.ActivityNavigatorOption,
) : ActivityNavigator {
    private val activityMessenger = UUID.randomUUID().toString()


    private var activityRef = WeakReference<Activity>(null)

    open fun createIntent(): ResultListenable<Intent?> {
        return listenable.map {
            val target = it.target
            when (target) {

                is Target.ActivityTarget ->{
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
                else -> {
                    with(it) {
                        val uri = ParameterSupport.getUriAsString(bundle)
                        val context = contextHolder.getContext()
                        var intent = Intent.parseUri(uri,0)
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
        launchIntentLazy.createUpon<Activity?> {
            if (it == null) {
                setResult(null)
                return@createUpon
            }
            val params = listenable.await()
            if (it.resolveActivity(params.contextHolder.getPackageManager()) == null) {
                setResult(null)
                return@createUpon
            }
            listenerActivityCreated(this)
            params.contextHolder.getContext().startActivity(it)
        }.listen {
            activityRef = WeakReference(it)
        }
    }

    private val requestActivityLazy: ResultListenable<Activity?>
        get() {
            return createRequestActivityListenable
        }

    private fun listenerActivityCreated(listenable: ResultSetter<Activity?>) {
        listenActivityCreated(activityMessenger, activityMessenger, listenable)
    }


    override fun getLaunchIntent(): ResultListenable<Intent?> {
        return launchIntentLazy
    }

    override fun navigateForActivityResult(activity: Activity): ResultListenable<ActivityResult> {
        val anchorActivityRef = WeakReference(activity)
        launchIntentLazy.createUpon<Activity?> {
            if (it == null) {
                setResult(null)
                return@createUpon
            }
            val params = listenable.await()
            if (it.resolveActivity(params.contextHolder.getPackageManager()) == null) {
                setResult(null)
                return@createUpon
            }
            listenerActivityCreated(this)
        }.listen {
            activityRef = WeakReference(it)
        }
        return launchIntentLazy.createUpon {
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


interface ActivityNavigator : Navigator {

    companion object {
        internal fun create(
            listenable: ResultListenable<ActivityNavigatorParams>,
            navigatorOption: NavigatorOption,
        ): ActivityNavigator {
            val activityNavigatorOption =
                navigatorOption as? NavigatorOption.ActivityNavigatorOption
                    ?: NavigatorOption.ActivityNavigatorOption()
            return ActivityNavigatorImpl(listenable, activityNavigatorOption)
        }
    }

    fun getLaunchIntent(): ResultListenable<Intent?>

    fun navigateForActivityResult(activity: Activity): ResultListenable<ActivityResult>


    fun requestActivity(): ResultListenable<Activity?>

    fun <T : Activity> requestInstanceActivity(clazz: Class<T>): ResultListenable<T>

}

