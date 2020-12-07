@file:Suppress("SafeCastWithReturn")

package com.x930073498.router.impl

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import com.x930073498.router.action.*
import com.x930073498.router.action.Target
import com.x930073498.router.impl.SystemActionDelegate.target
import com.x930073498.router.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class ActionType {
    object ACTIVITY : ActionType()
    object FRAGMENT : ActionType()
    object METHOD : ActionType()
    object SERVICE : ActionType()
    object INTERCEPTOR : ActionType()
    internal object SYSTEM : ActionType()
    internal object NONE : ActionType()
}

interface ActionDelegate {
    fun type(): ActionType
    val path: String
    fun parentPath(): String {
        return ""
    }

    val group: String
        get() = ""

    fun interceptors(): List<String> = arrayListOf()

}


internal object EmptyDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.NONE
    }

    override val path: String
        get() = ""


}

internal object SystemActionDelegate : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.SYSTEM
    }

    override val path: String
        get() = ""

    fun target() = Target.SystemTarget
}


@Suppress("UNCHECKED_CAST")
suspend fun ActionDelegate.navigate(bundle: Bundle, contextHolder: ContextHolder): Any? {
    ParameterSupport.putCenter(bundle, path)
    return when (this) {
        EmptyDelegate -> {
            return null
        }
        SystemActionDelegate -> {
            val target = target()
            target.go(
                contextHolder.getContext(),
                bundle,
                ParameterSupport.getUriAsString(bundle)
            )
            null
        }
        is FragmentActionDelegate -> {
            if (Looper.getMainLooper() == Looper.getMainLooper()) {

                fun inject(fragment: Fragment) {
                    inject(bundle, fragment)
                    var parentPath = parentPath()
                    var action: FragmentActionDelegate?
                    while (parentPath.isNotEmpty()) {
                        action = ActionCenter.getAction(parentPath) as? FragmentActionDelegate
                        if (action != null) {
                            parentPath = action.parentPath()
                            action.inject(bundle, fragment)
                        } else {
                            parentPath = ""
                        }
                    }
                }

                factory().create(contextHolder, target().targetClazz, bundle).apply {
                    inject(this)
                }
            } else {
                withContext(Dispatchers.Main.immediate) {
                    factory().create(contextHolder, target().targetClazz, bundle).apply {
                        inject(bundle, this)
                    }
                }
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

            fun inject(iService: IService) {
                inject(bundle, iService)
                var parentPath = parentPath()
                var action: ServiceActionDelegate?
                while (parentPath.isNotEmpty()) {
                    action = ActionCenter.getAction(parentPath) as? ServiceActionDelegate
                    if (action != null) {
                        parentPath = action?.parentPath() ?: ""
                        action?.inject(bundle, iService)
                    } else {
                        parentPath = ""
                    }
                }
            }
            if (result is IService) {
                result.init(contextHolder, bundle)
                inject(result)
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
        is InterceptorActionDelegate -> {
            factory().create(contextHolder, target().targetClazz)
        }
        else -> null

    }
}


