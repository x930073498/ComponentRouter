package com.x930073498.component.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Response
import com.x930073498.component.router.navigator.*
import com.x930073498.component.router.util.ParameterSupport
import java.lang.RuntimeException

interface RouterResponse : Response {
    val uri: Uri
    val bundle: Bundle
    val contextHolder: ContextHolder
    val isSuccessful: Boolean//表示执行成功


    companion object {
        val Empty = object : RouterResponse {
            override val uri: Uri
                get() = Uri.EMPTY
            override val bundle: Bundle
                get() = Bundle.EMPTY
            override val contextHolder: ContextHolder
                get() = ContextHolder.create()
            override val isSuccessful: Boolean
                get() = true

        }
    }
}

fun RouterResponse.success(isSuccess: Boolean = true): RouterResponse {
    return InternalResponse(this).also { it.mSuccess = isSuccess }
}

internal fun RouterResponse.asNavigateParams(): NavigateParams {
    return NavigateParams(bundle, contextHolder)
}


suspend fun RouterResponse.navigate(): Any? {
    return asNavigator().navigate()
}

fun RouterResponse.asNavigator(): Navigator {
    val action = ActionCenter.getAction(uri)
    return when (val target = action.target) {
        is Target.ServiceTarget -> ServiceNavigator.create(target, contextHolder, bundle)
        is Target.MethodTarget -> MethodNavigator.create(target, contextHolder, bundle)
        is Target.ActivityTarget -> ActivityNavigator.create(target, contextHolder, bundle)
        is Target.FragmentTarget -> FragmentNavigator.create(target, contextHolder, bundle)
        is Target.InterceptorTarget -> InterceptorNavigator.create(target,contextHolder, bundle)
        is Target.SystemTarget -> SystemActionHolder.create(target, contextHolder, bundle)
    }
}

fun RouterResponse.asActivity(): ActivityNavigator {
    return runCatching { asNavigator() as ActivityNavigator }.getOrElse {
        throw RuntimeException("当前路由结果不是一个Activity")
    }
}

fun RouterResponse.asFragment(): FragmentNavigator {
    return runCatching { asNavigator() as FragmentNavigator }.getOrElse {
        throw RuntimeException("当前路由结果不是一个fragment")
    }
}

fun RouterResponse.asMethod(): MethodNavigator {
    return runCatching { asNavigator() as MethodNavigator }.getOrElse {
        throw RuntimeException("当前路由结果不是一个方法")
    }

}

fun RouterResponse.asService(): ServiceNavigator {
    return runCatching { asNavigator() as ServiceNavigator }.getOrElse {
        throw RuntimeException("当前路由结果不是一个IService")
    }
}

fun RouterResponse.asActionDelegate(): ActionDelegate {
    return ActionCenter.getAction(uri).apply {
        ParameterSupport.putCenter(bundle, path)
    }
}




