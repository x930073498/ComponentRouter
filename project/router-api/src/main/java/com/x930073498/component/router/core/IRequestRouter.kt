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

sealed class DirectRequestResult(
    open val result: Any?,
    open val action: ActionDelegate?,
    val bundle: Bundle,
    val contextHolder: ContextHolder
) {
    class MethodResult internal constructor(
        override val result: MethodInvoker,
        override val action: MethodActionDelegate,
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(result, action, bundle, contextHolder)

    class FragmentResult internal constructor(
        override val result: Fragment,
        override val action: FragmentActionDelegate,
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(result, action, bundle, contextHolder) {
        fun <T : Fragment> getFragment(): T {
            return result as T
        }

    }

    class ServiceResult internal constructor(
        override val result: IService,
        override val action: ServiceActionDelegate,
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(result, action, bundle, contextHolder) {
        fun <T> getService(): T {
            return result as T
        }

    }

    class InterceptorResult internal constructor(
        override val result: RouterInterceptor,
        override val action: InterceptorActionDelegate,
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(result, action, bundle, contextHolder)

    class ActivityResult internal constructor(
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(null, null, bundle, contextHolder)

    class Empty internal constructor(
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : DirectRequestResult(null, null, bundle, contextHolder)

    object Ignore : DirectRequestResult(null, null, bundleOf(), ContextHolder.create())

}