package com.x930073498.router.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

        internal fun  getMethod(clazz: Class<*>):Any?{
            return methodMap[clazz]
        }


    }

    class ServiceTarget(targetClazz: Class<*>, val isSingleTon: Boolean) :
        Target(targetClazz)

    class MethodTarget(targetClazz: Class<*>) :
        Target(targetClazz)


    class ActivityTarget(targetClazz: Class<*>) : Target(targetClazz)

    class FragmentTarget(targetClazz: Class<*>) : Target(targetClazz)

    internal class SystemTarget internal constructor(val path: String?) : Target(Unit::class.java) {

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





