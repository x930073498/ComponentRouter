package com.x930073498.router.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.x930073498.router.impl.MethodInvoker

@Suppress("UNCHECKED_CAST")
sealed class Target(
    val targetClazz: Class<*>,
) {
    companion object {
        private val providerMap = mutableMapOf<Class<*>, Any?>()
        private val methodMap = mutableMapOf<Class<*>, Any?>()
        internal fun putProviderSingleton(clazz: Class<*>, any: Any?) {
            providerMap[clazz] = any
        }

        internal fun <T> getProviderSingleton(clazz: Class<T>): T? {
            return providerMap[clazz] as? T
        }

        internal fun putMethod(clazz: Class<*>, any: Any?) {
            methodMap[clazz] = any

        }

        internal fun getMethod(clazz: Class<*>): Any? {
            return methodMap[clazz]
        }


    }

    class ServiceTarget(targetClazz: Class<*>, val isSingleTon: Boolean) :
        Target(targetClazz)

    class MethodTarget(targetClazz: Class<*>) :
        Target(targetClazz)


    class ActivityTarget(targetClazz: Class<*>) : Target(targetClazz)

    class FragmentTarget(targetClazz: Class<*>) : Target(targetClazz)

    class InterceptorTarget(targetClazz: Class<*>) : Target(targetClazz)

    internal object SystemTarget : Target(Unit::class.java) {
        fun go(context: Context, bundle: Bundle, uri: String?) {
            var intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
            val info = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (info != null) {
                if (info.activityInfo.packageName != context.packageName) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(uri)
            intent.putExtras(bundle)
            runCatching {
                context.startActivity(intent)
            }.onFailure { it.printStackTrace() }
        }
    }
}





