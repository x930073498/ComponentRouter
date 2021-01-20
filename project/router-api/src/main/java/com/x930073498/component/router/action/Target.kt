package com.x930073498.component.router.action

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.annotation.CallSuper
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.Router
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.Interceptor
import com.x930073498.component.router.thread.IThread
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

@Suppress("UNCHECKED_CAST")
sealed class Target(
    val targetClazz: Class<*>
) {

    class ServiceTarget(
        targetClazz: Class<*>,
        internal val isSingleTon: Boolean,
        val action: ServiceActionDelegate
    ) :
        Target(targetClazz)


    class MethodTarget(targetClazz: Class<*>, val action: MethodActionDelegate) :
        Target(targetClazz)


    class ActivityTarget(targetClazz: Class<*>, val action: ActivityActionDelegate) :
        Target(targetClazz)

    class FragmentTarget(targetClazz: Class<*>, val action: FragmentActionDelegate) :
        Target(targetClazz)

    class InterceptorTarget(targetClazz: Class<*>, val action: InterceptorActionDelegate) :
        Target(targetClazz)

    internal class SystemTarget :
        Target(Unit::class.java)
}






