@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ModuleHandle
import com.x930073498.component.router.core.*
import com.x930073498.component.router.core.RouterImpl
import com.x930073498.component.router.impl.InterceptorActionDelegate
import com.x930073498.component.router.impl.RouterInterceptor
import kotlin.properties.Delegates

internal val globalInterceptors = arrayListOf<Any>()

internal var fragmentPropertyAutoInject = true
internal var activityPropertyAutoInject = true

object Router : InitI, ModuleHandle by ActionCenter.moduleHandler,PropertyInjector by PropertyInjectorImpl() {

    fun ofHandle(): ModuleHandle {
        return ActionCenter.moduleHandler
    }


    fun addGlobalInterceptor(vararg interceptor: RouterInterceptor) {
        globalInterceptors.addAll(interceptor.asList())
    }

    internal fun addGlobalInterceptor(vararg interceptor: InterceptorActionDelegate) {
        globalInterceptors.addAll(interceptor.asList())
    }

    fun addGlobalInterceptor(vararg path: String) {
        globalInterceptors.addAll(path.asList())
    }

    internal var app by Delegates.notNull<Application>()
    internal var hasInit = false

    @Synchronized
    override fun init(app: Application): InitI {
        if (hasInit) return this
        this.app = app
        hasInit = true
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

    fun from(intent: Intent): IRequestRouter {
        return from(intent.toUri(0))
    }

}








