package com.x930073498.component.router

import android.content.Context
import com.x930073498.component.auto.ConfigurationHolder
import com.x930073498.component.auto.getAction
import com.x930073498.component.router.core.DirectRequestResult
import com.x930073498.component.router.core.IRequestRouter
import com.x930073498.component.router.core.IRouterHandler
import com.x930073498.component.router.core.InitI
import com.x930073498.component.router.coroutines.AwaitResultCoroutineScope
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.navigator.*
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.asNavigator
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

var router_default_debounce=600L

fun ConfigurationHolder.byRouter(action: InitI.() -> Unit) {
    push(Router)
    getAction<InitI>()?.apply(action)
}

fun IRequestRouter.request(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<RouterResponse> {
    return requestInternal(scope, coroutineContext, debounce, context, request)
}

/**
 * 忽略所有的拦截器直接请求
 */
fun IRequestRouter.requestDirect(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult {
    return requestInternalDirect(debounce, context, request)
}

fun IRequestRouter.requestDirectAsService(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.ServiceResult? {
    return requestInternalDirect(debounce, context, request) as? DirectRequestResult.ServiceResult
}

fun IRequestRouter.requestDirectAsFragment(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.FragmentResult? {
    return requestInternalDirect(debounce, context, request) as? DirectRequestResult.FragmentResult
}

fun IRequestRouter.requestDirectAsMethod(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.MethodResult? {
    return requestInternalDirect(debounce, context, request) as? DirectRequestResult.MethodResult
}

fun IRequestRouter.requestDirectAsActivity(
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: IRouterHandler.() -> Unit = {}
): DirectRequestResult.ActivityResult? {
    return requestInternalDirect(debounce, context, request) as? DirectRequestResult.ActivityResult
}

fun IRequestRouter.navigate(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<NavigatorResult> {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .navigate()
}


fun IRequestRouter.asNavigator(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): Navigator {
    return requestInternal(scope, coroutineContext, debounce, context, request).asNavigator()
}


fun IRequestRouter.asActivity(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.ActivityNavigatorOption = NavigatorOption.ActivityNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ActivityNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request).asNavigator()
        .asActivity(navigatorOption)
}


fun IRequestRouter.asFragment(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
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
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
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
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.ServiceNavigatorOption = NavigatorOption.ServiceNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ServiceNavigator {
    return requestInternal(scope, coroutineContext, debounce, context, request)
        .asNavigator()
        .asService(navigatorOption)
}

suspend fun IRequestRouter.scopeNavigate(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<NavigatorResult> {
    return requestInternal(coroutineContext, debounce, context, request)
        .asNavigator()
        .navigate()
}

suspend fun IRequestRouter.scopeActivity(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.ActivityNavigatorOption = NavigatorOption.ActivityNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ActivityNavigator {
    return requestInternal(coroutineContext, debounce, context, request).asNavigator()
        .asActivity(navigatorOption)
}

suspend fun IRequestRouter.scopeNavigator(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): Navigator {
    return requestInternal(coroutineContext, debounce, context, request).asNavigator()
}

suspend fun IRequestRouter.scopeFragment(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.FragmentNavigatorOption = NavigatorOption.FragmentNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): FragmentNavigator {
    return requestInternal(coroutineContext, debounce, context, request)
        .asNavigator()
        .asFragment(navigatorOption)
}

suspend fun IRequestRouter.scopeService(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.ServiceNavigatorOption = NavigatorOption.ServiceNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ServiceNavigator {
    return requestInternal(coroutineContext, debounce, context, request)
        .asNavigator()
        .asService(navigatorOption)
}

suspend fun IRequestRouter.scopeMethod(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.MethodNavigatorOption = NavigatorOption.MethodNavigatorOption(),
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): MethodNavigator {
    return requestInternal(coroutineContext, debounce, context, request)
        .asNavigator()
        .asMethod(navigatorOption)
}

suspend fun IRequestRouter.scopeRequest(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = router_default_debounce,
    context: Context? = null,
    request: suspend IRouterHandler.() -> Unit = {}
): ResultListenable<RouterResponse> {
    return requestInternal(coroutineContext, debounce, context, request)
}