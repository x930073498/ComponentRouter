package com.x930073498.component.router.navigator

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.LaunchMode
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.cast
import com.x930073498.component.router.coroutines.flatMap
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.RouterInterceptor
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.thread.IThread


interface Navigator {
    fun navigate(): ResultListenable<NavigatorResult>
}

sealed class NavigatorOption {
    object Empty : NavigatorOption()
    class ServiceNavigatorOption(val autoInvoke: Boolean? = null, val singleton: Boolean? = null) :
        NavigatorOption()

    class MethodNavigatorOption(val thread: IThread? = null) : NavigatorOption()
    class ActivityNavigatorOption(val launchMode: LaunchMode?=null) : NavigatorOption()
    class FragmentNavigatorOption : NavigatorOption()
}

sealed class NavigatorParams(
    open val target: Target,
    val contextHolder: ContextHolder,
    val bundle: Bundle
) {
    fun mapToNavigator(
        parent: ResultListenable<NavigatorParams>,
        navigatorOption: NavigatorOption
    ): Navigator {
        return when (target) {
            is Target.ServiceTarget -> ServiceNavigator.create(parent.cast(), navigatorOption)
            is Target.MethodTarget -> MethodNavigator.create(parent.cast(), navigatorOption)
            is Target.ActivityTarget -> ActivityNavigator.create(parent.cast(), navigatorOption)
            is Target.FragmentTarget -> FragmentNavigator.create(parent.cast(), navigatorOption)
            is Target.InterceptorTarget -> InterceptorNavigator.create(parent.cast())
            is Target.SystemTarget -> ActivityNavigator.create(parent.cast(), navigatorOption)
        }

    }


}

internal class ServiceNavigatorParams(
    override val target: Target.ServiceTarget,
    contextHolder: ContextHolder,
    bundle: Bundle
) :
    NavigatorParams(target, contextHolder, bundle)

internal open class ActivityNavigatorParams(
    override val target: Target,
    contextHolder: ContextHolder,
    bundle: Bundle
) :
    NavigatorParams(target, contextHolder, bundle)

internal class MethodNavigatorParams(
    override val target: Target.MethodTarget,
    contextHolder: ContextHolder,
    bundle: Bundle
) :
    NavigatorParams(target, contextHolder, bundle)

internal class FragmentNavigatorParams(
    override val target: Target.FragmentTarget,
    contextHolder: ContextHolder,
    bundle: Bundle
) :
    NavigatorParams(target, contextHolder, bundle)


internal class InterceptorNavigatorParams(
    override val target: Target.InterceptorTarget,
    contextHolder: ContextHolder,
    bundle: Bundle
) :
    NavigatorParams(target, contextHolder, bundle)


class DispatcherNavigator internal constructor(
    private val listenable: ResultListenable<RouterResponse>,
    private val navigatorOption: NavigatorOption = NavigatorOption.Empty
) : Navigator {
    private val navigatorParamsListenable by lazy {
        listenable.createUpon<NavigatorParams> {
            val action = ActionCenter.getAction(it.uri)
            val result = when (val target = action.target) {
                is Target.ServiceTarget -> ServiceNavigatorParams(
                    target,
                    it.contextHolder,
                    it.bundle
                )
                is Target.MethodTarget -> MethodNavigatorParams(target, it.contextHolder, it.bundle)
                is Target.ActivityTarget -> ActivityNavigatorParams(
                    target,
                    it.contextHolder,
                    it.bundle
                )
                is Target.FragmentTarget -> FragmentNavigatorParams(
                    target,
                    it.contextHolder,
                    it.bundle
                )
                is Target.InterceptorTarget -> InterceptorNavigatorParams(
                    target,
                    it.contextHolder,
                    it.bundle
                )
                is Target.SystemTarget -> ActivityNavigatorParams(
                    target,
                    it.contextHolder,
                    it.bundle
                )
            }
            setResult(result)
        }
    }


    internal fun asActivity(activityNavigatorOption: NavigatorOption.ActivityNavigatorOption? = null): ActivityNavigator {
        return ActivityNavigator.create(
            navigatorParamsListenable.cast(),
            activityNavigatorOption ?: navigatorOption
        )
    }

    internal fun asFragment(fragmentNavigatorOption: NavigatorOption.FragmentNavigatorOption? = null): FragmentNavigator {
        return FragmentNavigator.create(
            navigatorParamsListenable.cast(),
            fragmentNavigatorOption ?: navigatorOption
        )
    }

    internal fun asMethod(methodNavigatorOption: NavigatorOption.MethodNavigatorOption? = null): MethodNavigator {
        return MethodNavigator.create(
            navigatorParamsListenable.cast(),
            methodNavigatorOption ?: navigatorOption
        )
    }
    internal fun asInterceptor(): InterceptorNavigator {
        return InterceptorNavigator.create(navigatorParamsListenable.cast())
    }

    internal fun asService(serviceNavigatorOption: NavigatorOption.ServiceNavigatorOption? = null): ServiceNavigator {
        return ServiceNavigator.create(
            navigatorParamsListenable.cast(),
            serviceNavigatorOption ?: navigatorOption
        )
    }

    override fun navigate(): ResultListenable<NavigatorResult> {
        return navigatorParamsListenable.map {
            it.mapToNavigator(navigatorParamsListenable, navigatorOption)
        }.flatMap {
            it.navigate()
        }
    }
}

inline fun <reified T : IService> NavigatorResult.SERVICE.getServiceInstance(): T {
    return getServiceInstance(T::class.java)
}


sealed class NavigatorResult {
    abstract fun getResult(): Any?

    fun asActivity(): ACTIVITY {
        return this as ACTIVITY
    }

    fun asFragment(): FRAGMENT {
        return this as FRAGMENT
    }

    fun asMethod(): METHOD {
        return this as METHOD
    }

    fun asService(): SERVICE {
        return this as SERVICE
    }

    class ACTIVITY(val activity: Activity?) : NavigatorResult() {
        override fun getResult(): Activity? {
            return activity
        }
    }

    class FRAGMENT(val fragment: Fragment?) : NavigatorResult() {
        override fun getResult(): Fragment? {
            return fragment
        }
    }

    class METHOD(
        @JvmField
        val result: Any?
    ) : NavigatorResult() {
        override fun getResult(): Any? {
            return result
        }

    }

    class SERVICE(
        val hasInvoke: Boolean,
        val service: IService,
        @JvmField
        val result: Any?
    ) :
        NavigatorResult() {
        override fun getResult(): Any? {
            if (hasInvoke) return result
            return service
        }

        fun <T : IService> getServiceInstance(clazz: Class<T>): T {
            return clazz.cast(service) as T
        }
    }


    class INTERCEPTOR(val interceptor: RouterInterceptor) : NavigatorResult() {
        override fun getResult(): RouterInterceptor {
            return interceptor
        }
    }
}





