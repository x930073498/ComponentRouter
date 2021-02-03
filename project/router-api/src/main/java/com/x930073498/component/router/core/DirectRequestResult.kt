package com.x930073498.component.router.core

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.impl.*

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