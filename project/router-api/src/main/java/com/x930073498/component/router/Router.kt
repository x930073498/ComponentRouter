@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.auto.getConfiguration
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.IApplicationLifecycle
import com.x930073498.component.core.IFragmentLifecycle
import com.x930073498.component.router.action.*
import com.x930073498.component.router.coroutines.*
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.onInterceptors
import com.x930073498.component.router.navigator.*
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.*
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates


class RouterInjectTask : IAuto, IActivityLifecycle, IApplicationLifecycle, IFragmentLifecycle {
    override fun onApplicationCreated(app: Application) {
        Router.init(app).apply {
            checkRouteUnique(getConfiguration().shouldRouterUnique())
        }
    }


    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        Router.inject(activity)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        Router.inject(f)
    }

}

interface ISerializerBundle {
    fun put(key: String, value: Any?)

    fun put(bundle: Bundle) {
        bundle.keySet().forEach {
            put(it, bundle.get(it))
        }
    }

    fun clear()

    companion object {
        internal fun createFormBundle(bundle: Bundle): ISerializerBundle {
            return object : ISerializerBundle {
                override fun put(key: String, value: Any?) {
                    bundle.putString(
                        ParameterSupport.getSerializerKey(key),
                        when (value) {
                            null -> null
                            is String -> value
                            else -> getSerializer().serialize(value)
                        }
                    )
                }

                override fun clear() {
                    bundle.clear()
                }

            }
        }
    }
}


interface IRouterHandler<T> where T : IRouterHandler<T> {
    fun greenChannel(): T
    fun scheme(scheme: String): T
    fun query(query: String): T
    fun path(path: String): T
    fun authority(authority: String): T
    fun appendQuery(key: String, value: String): T
    fun uri(action: Uri.Builder.() -> Unit): T
    fun serializer(action: ISerializerBundle.() -> Unit): T
    fun bundle(action: Bundle.() -> Unit): T
    fun put(key: String, value: Any?): T
}


class Router internal constructor(private val mHandler: InternalRouterHandler) :
    IRouterHandler<Router> by mHandler {

    constructor(uri: Uri = Uri.EMPTY, activity: Activity? = null) : this(
        InternalRouterHandler(
            uri,
            activity
        )
    )

    init {
        mHandler.router = this
    }


    fun navigate(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext(),
    ): ResultListenable<NavigatorResult> {
        return requestInternal(scope, coroutineContext, context)
            .asNavigator()
            .navigate()
    }


    fun asNavigator(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): Navigator {
        return requestInternal(scope, coroutineContext, context).asNavigator()
    }

    fun asActivity(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): ActivityNavigator {
        return requestInternal(scope, coroutineContext, context).asNavigator().asActivity()
    }

    fun asFragment(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): FragmentNavigator {
        return requestInternal(scope, coroutineContext, context).asNavigator().asFragment()
    }

    fun asMethod(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): MethodNavigator {
        return requestInternal(scope, coroutineContext, context).asNavigator().asMethod()
    }

    fun asService(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): ServiceNavigator {
        return requestInternal(scope, coroutineContext, context).asNavigator().asService()
    }


    private fun requestInternal(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): ResultListenable<RouterResponse> {
        return createAwaitResult(scope, coroutineContext) {
            routerRequest(mHandler.uriBuilder.build(), mHandler.mBundle, context)
                .onInterceptors {
                    val request = request()
                    routerResponse(request.uri, request.bundle, request.contextHolder)
                }.beforeIntercept {
                    request().syncUriToBundle()
                }.add(ActionDelegateRouterInterceptor())
                .apply {
                    if (!mHandler.greenChannel) add(GlobalInterceptor)
                }
                .start()
        }
    }

    fun request(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        context: Context? = mHandler.contextHolder.getContext()
    ): ResultListenable<RouterResponse> {
        return requestInternal(scope, coroutineContext, context)
    }


    companion object Init : InitI, ModuleHandle by ActionCenter.moduleHandler {

        fun ofHandle(): ModuleHandle {
            return ActionCenter.moduleHandler
        }


        private val globalInterceptors = arrayListOf<Any>()


        fun addGlobalInterceptor(vararg interceptor: RouterInterceptor) {
            globalInterceptors.addAll(interceptor.asList())
        }

        internal fun addGlobalInterceptor(vararg interceptor: InterceptorActionDelegate) {
            globalInterceptors.addAll(interceptor.asList())
        }

        fun addGlobalInterceptor(vararg path: String) {
            globalInterceptors.addAll(path.asList())
        }


        internal object GlobalInterceptor : RouterInterceptor {
            override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
                val request = chain.request()
                globalInterceptors.reversed().mapNotNull {
                    when (it) {
                        is String -> from(it).asNavigator().navigate().await()
                            .getResult() as? RouterInterceptor
                        is RouterInterceptor -> it
                        is InterceptorActionDelegate -> it.factory()
                            .create(request.contextHolder, it.target.targetClazz)
                        else -> null
                    }
                }.forEach { chain.addNext(it) }
                return chain.process(request)
            }

        }

        internal fun <T> inject(activity: T) where T : Activity {
            val intent = activity.intent ?: return
            val key = ParameterSupport.getCenterKey(intent) ?: return
            val center = ActionCenter.getAction(key)
            val bundle = intent.extras ?: return
            (center as? ActivityActionDelegate)?.apply {
                inject(bundle, activity)
            }
        }

        internal fun <T> inject(fragment: T) where T : Fragment {
            val bundle = fragment.arguments ?: return
            val key = ParameterSupport.getCenterKey(bundle) ?: return
            val center = ActionCenter.getAction(key)
            (center as? FragmentActionDelegate)?.apply {
                inject(bundle, fragment)
            }
        }


        internal var app by Delegates.notNull<Application>()
        internal var hasInit = false

        @Synchronized
        override fun init(app: Application): InitI {
            if (hasInit) return this
            this.app = app
            hasInit = true
            return this
        }


        @Synchronized
        override fun checkRouteUnique(checkKeyUnique: Boolean): InitI {
            ActionCenter.checkKeyUnique = checkKeyUnique
            LogUtil.log("路由${if (checkKeyUnique) "会" else "不会"}检验唯一性")
            return this
        }

        fun from(uri: Uri): Router {
            return Router(uri)
        }


        inline fun <reified T> getServiceSync(): T? where T : IService {
            return ActionCenter.getService(T::class.java)
        }


        fun from(url: String): Router {
            return from(Uri.parse(url))
        }

        fun create(): Router {
            return from(Uri.EMPTY)
        }

    }
}

internal class InternalRouterHandler(uri: Uri = Uri.EMPTY, activity: Activity? = null) :
    IRouterHandler<Router> {
    var router: Router by Delegates.notNull()
    internal var uriBuilder = uri.buildUpon()
    internal val mBundle = bundleOf()

    internal var greenChannel = false
    private val iBundle = ISerializerBundle.createFormBundle(mBundle)

    internal val contextHolder = ContextHolder.create(activity)
    override fun greenChannel(): Router {
        this.greenChannel = true
        return router
    }

    override fun scheme(scheme: String): Router {
        uriBuilder.scheme(scheme)
        return router
    }

    override fun query(query: String): Router {
        uriBuilder.query(query)
        return router
    }

    override fun path(path: String): Router {
        uriBuilder.path(path)
        return router
    }

    override fun authority(authority: String): Router {
        uriBuilder.authority(authority)
        return router
    }

    override fun appendQuery(key: String, value: String): Router {
        uriBuilder.appendQueryParameter(key, value)
        return router
    }


    override fun uri(action: Uri.Builder.() -> Unit): Router {
        action(uriBuilder)
        return router
    }

    override fun serializer(action: ISerializerBundle.() -> Unit): Router {
        action(iBundle)
        return router
    }

    override fun bundle(action: Bundle.() -> Unit): Router {
        action(mBundle)
        return router
    }

    override fun put(key: String, value: Any?): Router {
        iBundle.put(key, value)
        return router
    }


}


interface InitI {
    fun init(app: Application): InitI

    fun checkRouteUnique(checkKeyUnique: Boolean): InitI
}






