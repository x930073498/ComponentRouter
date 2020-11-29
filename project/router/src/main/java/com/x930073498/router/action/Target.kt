package com.x930073498.router.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.x930073498.router.impl.MethodInvoker

@Suppress("UNCHECKED_CAST")
sealed class Target<T>(
    val targetClazz: Class<T>,
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

        internal fun <T> getMethod(clazz: Class<T>): T? where T : MethodInvoker<*> {
            return methodMap[clazz] as? T
        }


    }

    class ServiceTarget<T>(targetClazz: Class<T>, val isSingleTon: Boolean) :
        Target<T>(targetClazz)

    class MethodTarget<T, R>(targetClazz: Class<T>, val methodInvokerType: Class<R>) :
        Target<T>(targetClazz) where R : MethodInvoker<T>


    class ActivityTarget<T>(targetClazz: Class<T>) : Target<T>(targetClazz)

    class FragmentTarget<T>(targetClazz: Class<T>) : Target<T>(targetClazz)

    internal class SystemTarget internal constructor(val path: String?) : Target<Unit>(Unit::class.java) {

        fun go(context: Context) {
            if (path.isNullOrEmpty()) return
            val intent = Intent.parseUri(path, Intent.URI_INTENT_SCHEME)
            val info = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (info != null) {
                if (info.activityInfo.packageName != context.packageName) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}





