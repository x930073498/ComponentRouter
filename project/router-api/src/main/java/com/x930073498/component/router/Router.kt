@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router

import android.content.Intent
import android.net.Uri
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


    fun from(uri: Uri): IRequestRouter {
        return RouterImpl(uri)
    }


    fun from(url: String): IRequestRouter {
        return from(Uri.parse(url))
    }


    fun <T> create(clazz: Class<T>): IClassRequestRouter<T> {
        return ClassRequestRouterImpl(clazz)
    }

    inline fun <reified T> create(): IClassRequestRouter<T> {
        return create(T::class.java)
    }

    fun from(intent: Intent): IRequestRouter {
        return from(intent.toUri(0))
    }

}








