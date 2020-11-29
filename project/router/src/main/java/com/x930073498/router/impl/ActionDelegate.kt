@file:Suppress("SafeCastWithReturn")

package com.x930073498.router.impl

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.router.Router
import com.x930073498.router.action.*
import com.x930073498.router.action.Target
import com.x930073498.router.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


interface ActionDelegate<T> {
    val path: String

    val group: String
        get() = ""

    suspend fun target(): Target<T>


}

internal class SystemActionDelegate(override val path: String) : ActionDelegate<Unit> {
    override suspend fun target(): Target<Unit> {
        return Target.SystemTarget(path)
    }

}


@Suppress("UNCHECKED_CAST")
suspend fun <T> ActionDelegate<T>.navigate(bundle: Bundle, contextHolder: ContextHolder): T? {
    val target = target()
    ParameterSupport.putCenter(bundle, path)
    return withContext(Dispatchers.Main) {
        when (this@navigate) {
            is SystemActionDelegate -> {
                target as Target.SystemTarget
                target.go(contextHolder.getContext())
                null
            }
            is FragmentActionDelegate<T> -> {
                val factory = factory() ?: object : FragmentActionDelegate.Factory<T> {
                    override suspend fun create(
                        contextHolder: ContextHolder,
                        clazz: Class<T>,
                        bundle: Bundle,
                    ): T? {
                        val t = target.targetClazz.newInstance()
                        if (t is Fragment) {
                            t.arguments = bundle
                        }
                        return t
                    }
                }
                factory.create(contextHolder, target.targetClazz, bundle)
            }
            is ActivityActionDelegate<T> -> {
                val context = contextHolder.getContext()
                val intent = Intent(context, target.targetClazz)
                if (context is Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.putExtras(bundle)
                context.startActivity(intent)
                null
            }
            is ServiceActionDelegate<T> -> {
                target as? Target.ServiceTarget<T> ?: return@withContext null
                val factory = factory() ?: object : ServiceActionDelegate.Factory<T> {
                    override suspend fun create(
                        contextHolder: ContextHolder,
                        clazz: Class<T>,
                        bundle: Bundle,
                    ): T? {
                        return clazz.newInstance()
                    }

                }
                val result = if (target.isSingleTon) {
                    Target.getProviderSingleton(target.targetClazz) ?: run {
                        factory.create(contextHolder, target.targetClazz, bundle).also {
                            Target.putProviderSingleton(target.targetClazz, it)
                        }
                    }
                } else {
                    factory.create(contextHolder, target.targetClazz, bundle)
                }
                if (result is IService) {
                    result.init(contextHolder, bundle)
                    Router.inject(result, bundle)
                    if (autoInvoke())
                        result.invoke()
                }
                result
            }
            else -> {
                this@navigate as? MethodActionDelegate<MethodInvoker<T>, T>
                    ?: return@withContext null
                target as? Target.MethodTarget<T, MethodInvoker<T>> ?: return@withContext null
                var invoker = Target.getMethod(target.methodInvokerType)
                val factory = factory() ?: object : MethodActionDelegate.Factory<MethodInvoker<T>> {
                    override suspend fun create(
                        contextHolder: ContextHolder,
                        clazz: Class<MethodInvoker<T>>,
                        bundle: Bundle,
                    ): MethodInvoker<T> {
                        return clazz.newInstance()
                    }
                }
                if (invoker == null) {
                    invoker = factory.create(contextHolder, target.methodInvokerType, bundle)
                    Target.putMethod(target.methodInvokerType, invoker)
                }
                invoker?.invoke(contextHolder, bundle)
            }
        }
    }
}


