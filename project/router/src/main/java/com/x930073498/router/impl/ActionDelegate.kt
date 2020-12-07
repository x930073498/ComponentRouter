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


interface ActionDelegate {
    val path: String

    val group: String
        get() = ""

    fun interceptors():List<String> = arrayListOf<String>()



}

internal object EmptyDelegate : ActionDelegate {
    override val path: String
        get() = ""


}

internal class SystemActionDelegate(override val path: String) : ActionDelegate {
    suspend fun target(): Target.SystemTarget {
        return Target.SystemTarget(path)
    }

}


@Suppress("UNCHECKED_CAST")
suspend fun ActionDelegate.navigate(bundle: Bundle, contextHolder: ContextHolder): Any? {

    ParameterSupport.putCenter(bundle, path)
    return withContext(Dispatchers.Main) {
        when (this@navigate) {
            EmptyDelegate -> {
                return@withContext null
            }
            is SystemActionDelegate -> {
                val target = target()
                target.go(contextHolder.getContext())
                null
            }
            is FragmentActionDelegate -> {
                factory().create(contextHolder, target().targetClazz, bundle).apply {
                    inject(bundle, this)
                }
            }
            is ActivityActionDelegate -> {
                val context = contextHolder.getContext()
                val intent = Intent(context, target().targetClazz)
                if (context is Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.putExtras(bundle)
                context.startActivity(intent)
                null
            }
            is ServiceActionDelegate -> {
                val target = target()
                val factory = factory()
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
                    inject(bundle, result)
                    if (autoInvoke())
                        result.invoke()
                }
                result
            }
            is MethodActionDelegate -> {
                val target = target()
                var invoker = Target.getMethod(target.targetClazz)
                val factory = factory()
                if (invoker == null) {
                    invoker = factory.create(contextHolder, target.targetClazz, bundle)
                    Target.putMethod(target.targetClazz, invoker)
                }
                if (invoker is MethodInvoker)
                    invoker.invoke(contextHolder, bundle)
                else null
            }
            is InterceptorActionDelegate->{
                factory().create(contextHolder,target().targetClazz)
            }
            else -> null
        }
    }
}


