package com.x930073498.router.action

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import com.x930073498.router.impl.*
import com.x930073498.router.util.ParameterSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
sealed class Target(
    val targetClazz: Class<*>
) {
    abstract suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any?

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

    class ServiceTarget(
        targetClazz: Class<*>,
        private val isSingleTon: Boolean,
        val action: ServiceActionDelegate
    ) :
        Target(targetClazz) {
        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any? {
            return action.run {
                val factory = factory()
                val result = if (isSingleTon) {
                    getProviderSingleton(targetClazz) ?: run {
                        factory.create(contextHolder, targetClazz, bundle).also {
                            putProviderSingleton(targetClazz, it)
                        }
                    }
                } else {
                    factory.create(contextHolder, targetClazz, bundle)
                }
                if (result is IService) {
                    result.init(contextHolder, bundle)
                    inject(bundle, result)
                    if (autoInvoke())
                        result.invoke()
                }
                result
            }
        }
    }

    class MethodTarget(targetClazz: Class<*>, val action: MethodActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any? {
            return action.run {
                var invoker = getMethod(targetClazz)
                if (invoker == null) {
                    val factory = factory()
                    invoker = factory.create(contextHolder, targetClazz, bundle)
                    putMethod(targetClazz, invoker)
                }
                if (invoker is MethodInvoker)
                    invoker.invoke(contextHolder, bundle)
                else null
            }
        }

    }


    class ActivityTarget(targetClazz: Class<*>, val action: ActivityActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any? {
            return action.run {
                val context = contextHolder.getContext()
                val intent = Intent(context,targetClazz)
                if (context is Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.putExtras(bundle)
                context.startActivity(intent)
                null
            }
        }

    }

    class FragmentTarget(targetClazz: Class<*>, val action: FragmentActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any {
            return action.run {
                if (Looper.getMainLooper() == Looper.getMainLooper()) {
                    factory().create(contextHolder, targetClazz, bundle).apply {
                        inject(bundle, this)
                    }
                } else {
                    withContext(Dispatchers.Main.immediate) {
                        factory().create(contextHolder, targetClazz, bundle)
                            .apply {
                                inject(bundle, this)
                            }
                    }
                }
            }
        }
    }

    class InterceptorTarget(targetClazz: Class<*>, val action: InterceptorActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any {
            return action.run {
                factory().create(contextHolder, targetClazz)
            }
        }
    }

    internal object SystemTarget : Target(Unit::class.java) {
        private fun go(context: Context, bundle: Bundle, uri: String?) {
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

        override suspend fun go(bundle: Bundle, contextHolder: ContextHolder): Any {
            return go(
                contextHolder.getContext(),
                bundle,
                ParameterSupport.getUriAsString(bundle)
            )
        }
    }
}





