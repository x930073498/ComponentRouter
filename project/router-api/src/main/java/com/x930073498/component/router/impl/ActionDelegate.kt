@file:Suppress("SafeCastWithReturn")

package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.router.action.NavigateInterceptor
import com.x930073498.component.router.action.NavigateParams
import com.x930073498.component.router.action.NavigateResult
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.thread.IThread
import com.x930073498.component.router.util.ParameterSupport


sealed class ActionType {
    object ACTIVITY : ActionType()
    object FRAGMENT : ActionType()
    object METHOD : ActionType()
    object SERVICE : ActionType()
    object INTERCEPTOR : ActionType()
    internal object SYSTEM : ActionType()
}

interface ActionDelegate {
    fun type(): ActionType
    val path: String
    val thread: IThread
        get() {
            return IThread.ANY
        }

    val target: Target


    fun inject(bundle: Bundle, target: Any)
    val group: String
        get() = ""

    fun interceptors(): List<String> = arrayListOf()


}


internal class SystemActionDelegate() : ActionDelegate {
    override fun type(): ActionType {
        return ActionType.SYSTEM
    }


    fun setNavigateInterceptor(navigateInterceptor: NavigateInterceptor?): ActionDelegate {
        _target.setNavigateInterceptor(navigateInterceptor)
        return this
    }

    private val _target = Target.SystemTarget()

    override val target: Target = _target

    override val path: String
        get() = ""

    override fun inject(bundle: Bundle, target: Any) {

    }
}


suspend fun ActionDelegate.getResult(
    navigateParams: NavigateParams,
    navigateInterceptor: NavigateInterceptor? = null,
    handler: ResultHandler = ResultHandler.Direct,
): Any? {
    val (bundle, contextHolder) = navigateParams
    ParameterSupport.putCenter(bundle, path)
    val params = NavigateParams(bundle, contextHolder)
    if (this is SystemActionDelegate) {
        setNavigateInterceptor(navigateInterceptor)
    }
    return handler.handle(target.go(params).result, params)
}

suspend fun ActionDelegate.navigate(
    navigateParams: NavigateParams,
    navigateInterceptor: NavigateInterceptor? = null,
): NavigateResult {
    val (bundle, contextHolder) = navigateParams
    ParameterSupport.putCenter(bundle, path)
    val params = NavigateParams(bundle, contextHolder)
    if (this is SystemActionDelegate) {
        setNavigateInterceptor(navigateInterceptor)
    }
    return target.go(params)
}



