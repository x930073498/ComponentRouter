@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.core.os.bundleOf
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.interceptor.onInterceptors
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.navigate
import com.x930073498.component.router.response.routerResponse
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.IApplicationLifecycle
import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.auto.getConfiguration
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.NavigateInterceptor
import com.x930073498.component.router.action.NavigateParams
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.request.RouterRequest
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates


class RouterInjectTask : IAuto, IActivityLifecycle, IApplicationLifecycle {
    override fun onApplicationCreated(app: Application) {
        Router.init(app).apply {
            checkRouteUnique(getConfiguration().shouldRouterUnique())
        }
    }


    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        Router.inject(activity)
    }


}


class Router(uri: Uri = Uri.EMPTY, activity: Activity? = null) {
    private var uriBuilder = uri.buildUpon()
    private val mBundle = bundleOf()

    private var greenChannel = false

    private val contextHolder = ContextHolder.create(activity)
    fun greenChannel() {
        this.greenChannel = true
    }

    fun scheme(scheme: String): Router {
        uriBuilder.scheme(scheme)
        return this
    }

    fun query(query: String): Router {
        uriBuilder.query(query)
        return this
    }

    fun path(path: String): Router {
        uriBuilder.path(path)
        return this
    }

    fun authority(authority: String): Router {
        uriBuilder.authority(authority)
        return this
    }

    fun appendQuery(key: String, value: String): Router {
        uriBuilder.appendQueryParameter(key, value)
        return this
    }


    fun uri(action: Uri.Builder.() -> Unit): Router {
        action(uriBuilder)
        return this
    }

    fun bundle(action: Bundle.() -> Unit): Router {
        action(mBundle)
        return this
    }

    fun bundle(bundle: Bundle): Router {
        mBundle.clear()
        mBundle.putAll(bundle)
        return this
    }

    fun put(key: String, value: Any?): Router {
        val bundle = bundleOf(key to value)
        mBundle.putAll(bundle)
        return this
    }


    suspend fun <T> navigate(context: Context? = null): T? {
        return forward(context) as? T
    }


    /**
     * 创建
     */
    fun <T> syncNavigation(context: Context? = null): T? {
        return forwardSync(context) as? T
    }

    /**
     * 主线程谨慎使用，因为可能会导致主线程卡住。
     */
    fun forwardSync(
        context: Context? = contextHolder.getContext(),
        navigateInterceptor: NavigateInterceptor? = null,
        handler: ResultHandler = ResultHandler.Direct
    ): Any? {
        val resultRef = AtomicReference<Any>()
        val flagRef = AtomicReference(0)
        val dispatcher =
            if (Looper.getMainLooper() == Looper.myLooper()) Dispatchers.Main.immediate else Dispatchers.IO
        GlobalScope.launch(dispatcher) {
            runCatching {
                val result = forward(context, navigateInterceptor, handler)
                resultRef.set(result)
            }.onFailure { it.printStackTrace() }
            flagRef.set(1)
        }
        while (flagRef.get() == 0) {
            //do nothing,Thread loop
        }
        return resultRef.get()
    }

    suspend fun forward(
        context: Context? = contextHolder.getContext(),
        navigateInterceptor: NavigateInterceptor? = null,
        handler: ResultHandler = ResultHandler.Direct
    ): Any? {
        return request(context).navigate(navigateInterceptor, handler)
    }


    suspend fun request(context: Context? = contextHolder.getContext()): RouterResponse {
        return routerRequest(uriBuilder.build(), mBundle, context)
            .onInterceptors {
                val request = request()
                routerResponse(request.uri, request.bundle, request.contextHolder)
            }.beforeIntercept {
                request().syncUriToBundle()
            }.add(ActionDelegateRouterInterceptor())
            .apply {
                if (!greenChannel) add(GlobalInterceptor)
            }

            .start()
    }


    companion object Init : InitI {


        private val globalInterceptors = arrayListOf<Any>()

        internal var navigateInterceptor: NavigateInterceptor? = null

        fun addGlobalInterceptor(vararg interceptor: RouterInterceptor) {
            globalInterceptors.addAll(interceptor.asList())
        }

        internal fun addGlobalInterceptor(vararg interceptor: InterceptorActionDelegate) {
            globalInterceptors.addAll(interceptor.asList())
        }

        fun addGlobalInterceptor(vararg path: String) {
            globalInterceptors.addAll(path.asList())
        }


        fun setNavigateInterceptor(interceptor: NavigateInterceptor) {
            navigateInterceptor = interceptor
        }


        internal object GlobalInterceptor : RouterInterceptor {
            override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
                val request = chain.request()
                globalInterceptors.reversed().mapNotNull {
                    when (it) {
                        is String -> ActionCenter.getAction(it)
                            .getResult(
                                NavigateParams(
                                    request.bundle,
                                    request.contextHolder
                                )
                            ) as? RouterInterceptor
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

        suspend inline fun <reified T> getService(): T? where T : IService {
            return ActionCenter.getService(T::class.java)
        }

        inline fun <reified T> getServiceSync(): T? where T : IService {
            return ActionCenter.getServiceSync(T::class.java)
        }


        fun from(url: String): Router {
            return from(Uri.parse(url))
        }

        fun create(): Router {
            return from(Uri.EMPTY)
        }

    }
}


interface InitI {
    fun init(app: Application): InitI

    fun checkRouteUnique(checkKeyUnique: Boolean): InitI
}






