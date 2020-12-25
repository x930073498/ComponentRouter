package com.x930073498.component.router.navigator

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.util.ParameterSupport

interface SystemActionHolder : ParameterProvider, Navigator {
    fun getLaunchIntent(): Intent?

    suspend fun navigateForActivityResult(): Intent

    companion object {
        internal fun create(
            target: Target.SystemTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): SystemActionHolder {
            return object : SystemActionHolder {
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

                override suspend fun navigateForActivityResult(): Intent {
                    TODO("Not yet implemented")
                }


                override fun getBundle(): Bundle {
                    return bundle
                }

                override fun getContextHolder(): ContextHolder {
                    return contextHolder
                }

                override suspend fun navigate(): Any? {
                    TODO("Not yet implemented")
                }

            }
        }
    }
}