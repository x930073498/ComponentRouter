package com.x930073498.component.router

import android.content.Context
import com.x930073498.component.auto.ConfigurationHolder
import com.x930073498.component.auto.getAction
import com.x930073498.component.router.core.IRequestRouter
import com.x930073498.component.router.core.InitI
import com.x930073498.component.router.coroutines.AwaitResultCoroutineScope
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.navigator.*
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.asNavigator
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

fun ConfigurationHolder.byRouter(action: InitI.() -> Unit) {
    push(Router)
    getAction<InitI>()?.apply(action)
}

fun IRequestRouter.request(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = 600L,
    context: Context? = null
): ResultListenable<RouterResponse> {
    return requestInternal(scope, coroutineContext, debounce, context)
}

fun IRequestRouter.navigate(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = 600L,
    context: Context? = null,
): ResultListenable<NavigatorResult> {
    return requestInternal(scope, coroutineContext, debounce, context)
        .asNavigator()
        .navigate()
}


fun IRequestRouter.asNavigator(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    debounce: Long = 600L,
    context: Context? = null
): Navigator {
    return requestInternal(scope, coroutineContext, debounce, context).asNavigator()
}


fun IRequestRouter.asActivity(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.ActivityNavigatorOption = NavigatorOption.ActivityNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): ActivityNavigator {
    return requestInternal(scope, coroutineContext, debounce, context).asNavigator()
        .asActivity(navigatorOption)
}


fun IRequestRouter.asFragment(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.FragmentNavigatorOption = NavigatorOption.FragmentNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): FragmentNavigator {
    return requestInternal(scope, coroutineContext, debounce, context)
        .asNavigator()
        .asFragment(navigatorOption)
}


fun IRequestRouter.asMethod(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.MethodNavigatorOption = NavigatorOption.MethodNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): MethodNavigator {
    return requestInternal(scope, coroutineContext, debounce, context)
        .asNavigator()
        .asMethod(navigatorOption)
}


fun IRequestRouter.asService(
    scope: CoroutineScope = AwaitResultCoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    navigatorOption: NavigatorOption.ServiceNavigatorOption = NavigatorOption.ServiceNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): ServiceNavigator {
    return requestInternal(scope, coroutineContext, debounce, context)
        .asNavigator()
        .asService(navigatorOption)
}

suspend fun IRequestRouter.scopeNavigate(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = 600L,
    context: Context? = null,
): ResultListenable<NavigatorResult> {
    return requestInternal(coroutineContext, debounce, context)
        .asNavigator()
        .navigate()
}

suspend fun IRequestRouter.scopeActivity(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.ActivityNavigatorOption = NavigatorOption.ActivityNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): ActivityNavigator {
    return requestInternal(coroutineContext, debounce, context).asNavigator()
        .asActivity(navigatorOption)
}

suspend fun IRequestRouter.scopeNavigator(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = 600L,
    context: Context? = null
): Navigator {
    return requestInternal(coroutineContext, debounce, context).asNavigator()
}

suspend fun IRequestRouter.scopeFragment(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.FragmentNavigatorOption = NavigatorOption.FragmentNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): FragmentNavigator {
    return requestInternal(coroutineContext, debounce, context)
        .asNavigator()
        .asFragment(navigatorOption)
}

suspend fun IRequestRouter.scopeService(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.ServiceNavigatorOption = NavigatorOption.ServiceNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): ServiceNavigator {
    return requestInternal(coroutineContext, debounce, context)
        .asNavigator()
        .asService(navigatorOption)
}

suspend fun IRequestRouter.scopeMethod(
    coroutineContext: CoroutineContext? = null,
    navigatorOption: NavigatorOption.MethodNavigatorOption = NavigatorOption.MethodNavigatorOption(),
    debounce: Long = 600L,
    context: Context? = null
): MethodNavigator {
    return requestInternal(coroutineContext, debounce, context)
        .asNavigator()
        .asMethod(navigatorOption)
}

suspend fun IRequestRouter.scopeRequest(
    coroutineContext: CoroutineContext? = null,
    debounce: Long = 600L,
    context: Context? = null
): ResultListenable<RouterResponse> {
    return requestInternal(coroutineContext, debounce, context)
}