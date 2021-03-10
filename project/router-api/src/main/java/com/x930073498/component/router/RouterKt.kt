@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.x930073498.component.router

import android.content.Context
import androidx.fragment.app.Fragment
import com.x930073498.component.auto.ConfigurationHolder
import com.x930073498.component.auto.getAction
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.core.*
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.MethodInvoker
import com.x930073498.component.router.navigator.*
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.asNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

var router_default_debounce = 600L

fun ConfigurationHolder.byRouter(action: InitI.() -> Unit) {
    push(Router)
    getAction<InitI>()?.apply(action)
}

fun IRequestRouter.request(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<RouterResponse> {
    return requestInternal(scope, coroutineContext, debounce, context, request)
}

fun <T> IClassRequestRouter<T>.request(
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): T? {
    return requestInternalWithClass(context, request)
}

fun <T> IClassRequestRouter<T>.requestService(
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): T? where T : IService {
    return requestInternalWithClass(context, request)
}


fun <T> IClassRequestRouter<T>.requestMethod(
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): T? where T : MethodInvoker {
    return requestInternalWithClass(context, request)
}


fun <T> IClassRequestRouter<T>.requestFragment(
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): T? where T : Fragment {
    return requestInternalWithClass(context, request)
}


/**
 * 忽略所有协程拦截器请求
 */
@Deprecated("不建议使用直接请求，以协程方式请求替代")
fun IRequestRouter.requestDirect(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult {
    val response = requestInternalDirect(debounce, context, request)
    if (response == RouterResponse.Empty) {
        return DirectRequestResult.Ignore
    }
    val contextHolder = ContextHolder.create(context)
    return ActionCenter.getResultDirect(response.uri, response.bundle, contextHolder)
}

fun IRequestRouter.requestDirectAsService(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.ServiceResult? {
    return requestDirect(debounce, context, request) as? DirectRequestResult.ServiceResult
}

internal fun IRequestRouter.requestDirectAsInterceptor(
    context: Context? = null,
): DirectRequestResult.InterceptorResult? {
    return requestDirect(-1, context) as? DirectRequestResult.InterceptorResult
}

@Deprecated("不建议使用直接请求，以协程方式请求替代")
fun IRequestRouter.requestDirectAsFragment(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.FragmentResult? {
    return requestDirect(debounce, context, request) as? DirectRequestResult.FragmentResult
}

@Deprecated("不建议使用直接请求，以协程方式请求替代")
fun IRequestRouter.requestDirectAsMethod(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.MethodResult? {
    return requestDirect(debounce, context, request) as? DirectRequestResult.MethodResult
}

@Deprecated("不建议使用直接请求，以协程方式请求替代")
fun IRequestRouter.requestDirectAsActivity(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.ActivityResult? {
    return requestDirect(debounce, context, request) as? DirectRequestResult.ActivityResult
}

fun IRequestRouter.navigate(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<NavigatorResult> {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .navigate()
}


fun IRequestRouter.asNavigator(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): Navigator {
    return requestInternal(scope, coroutineContext, debounce, context, request).asNavigator()
}


fun IRequestRouter.asActivity(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    navigatorOption: NavigatorOption.ActivityNavigatorOption = NavigatorOption.ActivityNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ActivityNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request).asNavigator()
        .asActivity(navigatorOption)
}


fun IRequestRouter.asFragment(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    navigatorOption: NavigatorOption.FragmentNavigatorOption = NavigatorOption.FragmentNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): FragmentNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .asFragment(navigatorOption)
}


fun IRequestRouter.asMethod(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    navigatorOption: NavigatorOption.MethodNavigatorOption = NavigatorOption.MethodNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): MethodNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .asMethod(navigatorOption)
}


fun IRequestRouter.asService(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    navigatorOption: NavigatorOption.ServiceNavigatorOption = NavigatorOption.ServiceNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ServiceNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .asService(navigatorOption)
}

