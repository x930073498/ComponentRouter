package com.x930073498.component.router.action

import android.app.Activity
import androidx.fragment.app.Fragment
import com.x930073498.component.router.impl.*

@Suppress("UNCHECKED_CAST")
sealed class Target(
    open val targetClazz: Class<*>
) {

    class ServiceTarget(
        override val targetClazz: Class<out IService>,
        internal val isSingleTon: Boolean,
        val action: ServiceActionDelegate
    ) :
        Target(targetClazz)


    class MethodTarget(
        override val targetClazz: Class<out MethodInvoker>,
        val action: MethodActionDelegate
    ) :
        Target(targetClazz)


    class ActivityTarget(
        override val targetClazz: Class<out Activity>,
        val action: ActivityActionDelegate
    ) :
        Target(targetClazz)

    class FragmentTarget(
        override val targetClazz: Class<out Fragment>,
        val action: FragmentActionDelegate
    ) :
        Target(targetClazz)

    class InterceptorTarget(
        override val targetClazz: Class<out RouterInterceptor>,
        val action: InterceptorActionDelegate
    ) :
        Target(targetClazz)

    internal class SystemTarget :
        Target(Unit::class.java)
}






