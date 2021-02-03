package com.x930073498.component.router.core

import android.content.Context
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.coroutines.AwaitResultCoroutineScope
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.router_default_debounce
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.response.RouterResponse
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

abstract class IClassRequestRouter<T> internal constructor(val clazz: Class<T>){

    @MainThread
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal abstract fun requestInternalWithClass(
        context: Context? = null,
        request: IRouterHandler.() -> Unit = {}
    ): T?
}



abstract class IRequestRouter internal constructor() {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal abstract suspend fun requestInternal(
        coroutineContext: CoroutineContext? = null,
        debounce: Long = router_default_debounce,
        context: Context? = null,
        request: suspend IRouterHandler.() -> Unit = {}
    ): ResultListenable<RouterResponse>

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal abstract fun requestInternal(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        debounce: Long = router_default_debounce,
        context: Context? = null,
        request: suspend IRouterHandler.() -> Unit = {}
    ): ResultListenable<RouterResponse>

    /**
     * 忽略所有的拦截器直接请求,必须在主线程请求
     */
    @MainThread
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal abstract fun requestInternalDirect(
        debounce: Long = router_default_debounce,
        context: Context? = null,
        request: IRouterHandler.() -> Unit = {}
    ): DirectRequestResult


}

