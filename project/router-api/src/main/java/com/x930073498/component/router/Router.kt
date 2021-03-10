@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ModuleHandle
import com.x930073498.component.router.core.*
import com.x930073498.component.router.impl.InterceptorActionDelegate

internal val globalInterceptors = arrayListOf<InterceptorActionDelegate>()

internal var fragmentPropertyAutoInject = true
internal var activityPropertyAutoInject = true

object Router : InitI, ModuleHandle by ActionCenter.moduleHandler,
    PropertyInjector by PropertyInjectorImpl() {

    @JvmStatic
    fun ofHandle(): ModuleHandle {
        return ActionCenter.moduleHandler
    }


    internal fun addGlobalInterceptor(vararg interceptor: InterceptorActionDelegate) {
        globalInterceptors.addAll(interceptor.asList())
    }


    @Synchronized
    override fun init(): InitI {
        return this
    }

    @Synchronized
    override fun checkRouteUnique(checkKeyUnique: Boolean): InitI {
        ActionCenter.checkKeyUnique = checkKeyUnique
        LogUtil.log("路由${if (checkKeyUnique) "会" else "不会"}检验唯一性")
        return this
    }

    override fun fragmentPropertyAutoInject(autoInjectProperty: Boolean): InitI {
        fragmentPropertyAutoInject = autoInjectProperty
        return this
    }

    override fun activityPropertyAutoInject(autoInjectProperty: Boolean): InitI {
        activityPropertyAutoInject = autoInjectProperty
        return this
    }


    @JvmOverloads
    @JvmStatic
    fun from(uri: Uri, bundle: Bundle = bundleOf()): IRequestRouter {
        return RouterImpl(uri, bundle)
    }


    @JvmStatic
    @JvmOverloads
    fun from(url: String, bundle: Bundle = bundleOf()): IRequestRouter {
        return from(Uri.parse(url), bundle)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> create(clazz: Class<T>, bundle: Bundle = bundleOf()): IClassRequestRouter<T> {
        return ClassRequestRouterImpl(clazz,bundle)
    }

    inline fun <reified T> create(bundle: Bundle = bundleOf()): IClassRequestRouter<T> {
        return create(T::class.java,bundle)
    }

    @JvmStatic
    fun from(intent: Intent): IRequestRouter {
        return from(intent.toUri(0), intent.extras ?: bundleOf())
    }

}








