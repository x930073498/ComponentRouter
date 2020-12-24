package com.x930073498.component.fragmentation

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.app
import com.x930073498.component.router.IRouterHandler
import com.x930073498.component.router.Router
import com.x930073498.component.router.action.NavigateParams
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.ActivityActionDelegate
import com.x930073498.component.router.impl.FragmentActionDelegate
import com.x930073498.component.router.impl.SystemActionDelegate
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.asActionDelegate
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun FragmentActionDelegate.asDestination(controller: NavController): NavDestination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return navigator.createDestination().apply {
        id = path.hashCode()
        className = target.targetClazz.name
        addDeepLink(path)
    }
}

private fun SystemActionDelegate.asDestination(
    controller: NavController,
    params: NavigateParams
): NavDestination? {
    val navigator = controller.navigatorProvider[ActivityNavigator::class]
    val bundle = params.bundle
    val uri = ParameterSupport.getUriAsString(bundle) ?: return null

    var intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
    var info = app.packageManager.resolveActivity(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    if (info != null) {
        if (info.activityInfo.packageName != app.packageName) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return navigator.createDestination().apply {
            id = uri.hashCode()
            setIntent(intent)
        }
    }
    intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(uri)
    intent.putExtras(bundle)

    info = app.packageManager.resolveActivity(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    with(info) {
        return if (this == null) {
            null
        } else {
            if (activityInfo.packageName != app.packageName) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            navigator.createDestination().apply {
                id = uri.hashCode()
                setIntent(intent)
            }
        }
    }


}

private fun ActivityActionDelegate.asDestination(controller: NavController): NavDestination {
    val navigator = controller.navigatorProvider[ActivityNavigator::class]
    return navigator.createDestination().apply {
        id = path.hashCode()
        setComponentName(ComponentName(app, target.targetClazz))
        addDeepLink(path)
    }
}

private fun ActionDelegate.asDestination(
    controller: NavController,
    params: NavigateParams
): NavDestination? {
    return when (this) {
        is FragmentActionDelegate -> asDestination(controller)
        is ActivityActionDelegate -> asDestination(controller)
        is SystemActionDelegate -> asDestination(controller, params)
        else -> null
    }
}

private fun RouterResponse.asDestination(controller: NavController): NavDestination? {
    return asActionDelegate().asDestination(controller, asNavigateParams())
}

private fun RouterResponse.asNavigateParams(): NavigateParams {
    return NavigateParams(bundle, contextHolder)
}

suspend fun Fragment.loadRootFromRouter(
    containerId: Int,
    path: String,
    action: IRouterHandler<*>.() -> Unit = {}
) {
    withContext(Dispatchers.Main) {
        val view = requireView().findViewById<View>(containerId) ?: return@withContext
        val controller = NavHostController(requireContext())
        controller.setLifecycleOwner(this@loadRootFromRouter)
        controller.setViewModelStore(viewModelStore)
        controller.setOnBackPressedDispatcher(requireActivity().onBackPressedDispatcher)
        controller.navigatorProvider.apply {
            addNavigator(
                FragmentNavigator(
                    requireContext(),
                    childFragmentManager,
                    containerId
                )
            )
            addNavigator(ActivityNavigator(requireContext()))
        }
        Navigation.setViewNavController(view, controller)
        controller.loadRootFromRouter(path, action)
    }
}

suspend fun FragmentActivity.loadRootFromRouter(
    containerId: Int,
    path: String,
    action: IRouterHandler<*>.() -> Unit = {}
) {
    withContext(Dispatchers.Main) {
        val view = findViewById<View>(containerId) ?: return@withContext
        val controller = NavHostController(this@loadRootFromRouter)
        controller.setLifecycleOwner(this@loadRootFromRouter)
        controller.setViewModelStore(viewModelStore)
        controller.setOnBackPressedDispatcher(onBackPressedDispatcher)
        controller.navigatorProvider.apply {
            addNavigator(
                FragmentNavigator(
                    this@loadRootFromRouter,
                    supportFragmentManager,
                    containerId
                )
            )
            addNavigator(ActivityNavigator(this@loadRootFromRouter))
        }
        Navigation.setViewNavController(view, controller)
        controller.loadRootFromRouter(path, action)
    }
}


suspend fun Fragment.startWithRouter(path: String, action: NavRouter.() -> Unit = {}) {
    val controller = runCatching {
        Navigation.findNavController(requireView())
    }.getOrNull() ?: run {
        LogUtil.log("请先调用loadRootFromRouter")
        return
    }
    val router = Router.from(path)
    val nav = NavRouter(router)
    action(nav)
    val response = router
        .request()
    val destination = response.asDestination(controller) ?: run {
        LogUtil.log("path:$path 没有对应的Router，请检查路由定义是否正确或者检查路由是否已经被卸载")
        return
    }
    val graph = controller.graph
    runCatching {
        graph[destination.id]
    }.onFailure { graph.addDestination(destination) }
    withContext(Dispatchers.Main) {
        controller.navigate(destination.id, response.asNavigateParams().bundle, nav.getNavOptions())
    }
}


fun Fragment.popSelf() {
    val path = Router.ofHandle().getRealPathFromTarget(this) ?: return
    popTo(path, true)
}

fun pathToDestinationId(path: String): Int? {
    return Uri.parse(path)?.path?.hashCode()
}


fun Fragment.popTo(path: String, inclusive: Boolean = false) {
    val id = pathToDestinationId(path) ?: return
    val controller =
        runCatching { Navigation.findNavController(requireView()) }.getOrNull() ?: run {
            LogUtil.log("请先调用loadRootFromRouter")
            return
        }
    controller.popBackStack(id, inclusive)
}

private suspend fun NavController.loadRootFromRouter(path: String, action: IRouterHandler<*>.() -> Unit = {}) {
    val response = Router.from(path)
        .apply { action() }
        .request()
    val destination = response.asDestination(this) ?: return
    val params = response.asNavigateParams()
    val mNavGraph = NavGraph(NavGraphNavigator(navigatorProvider))
    mNavGraph.addDestination(destination)
    mNavGraph.startDestination = destination.id
    mNavGraph.id = pathToDestinationId("nav") ?: 1
    withContext(Dispatchers.Main) {
        setGraph(mNavGraph, params.bundle)
    }

}

var animBuilderAction: AnimBuilder.() -> Unit = {
    enter = R.anim.v_fragment_enter
    exit = R.anim.v_fragment_exit
//    popEnter = R.anim.h_fragment_pop_enter
    popEnter = R.anim.v_fragment_enter
//    popExit = R.anim.h_fragment_pop_exit
    popExit = R.anim.v_fragment_exit
}

class NavRouterOptionsBuilder internal constructor(private val builder: NavOptionsBuilder) {

    var launchSingleTop = false
        set(value) {
            builder.launchSingleTop = value
            field = value
        }
    var popUpTo: String = ""
        set(value) {
            field = value
            val id = pathToDestinationId(value)
            if (id != null) {
                builder.popUpTo = id
            }
        }


    fun popUpTo(id: String, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
        val destinationId = pathToDestinationId(id) ?: return
        builder.popUpTo(destinationId, popUpToBuilder)
    }

    fun anim(animBuilder: AnimBuilder.() -> Unit) {
        builder.anim(animBuilder)
    }
}

class NavRouter internal constructor(private val router: IRouterHandler<*>) {
    private var navAction: NavRouterOptionsBuilder.() -> Unit = {}

    fun withRouter(action: IRouterHandler<*>.() -> Unit = {}) {
        action(router)
    }

    fun withNavOptions(action: NavRouterOptionsBuilder.() -> Unit = {}) {
        navAction = action
    }

    internal fun getNavOptions(): NavOptions {
        return navOptions {
            NavRouterOptionsBuilder(this).apply {
                anim(animBuilderAction)
                navAction()
            }
        }
    }


}