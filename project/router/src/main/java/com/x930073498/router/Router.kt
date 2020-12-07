@file:Suppress("UNCHECKED_CAST")

package com.x930073498.router

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.impl.ActionDelegateRouterInterceptor
import com.x930073498.router.impl.ActivityActionDelegate
import com.x930073498.router.impl.FragmentActionDelegate
import com.x930073498.router.interceptor.onInterceptors
import com.x930073498.router.request.routerRequest
import com.x930073498.router.response.RouterResponse
import com.x930073498.router.response.navigate
import com.x930073498.router.response.routerResponse
import com.x930073498.router.util.ParameterSupport
import com.zx.common.auto.IActivityLifecycle
import com.zx.common.auto.IApplicationLifecycle
import com.zx.common.auto.IAuto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates


class RouterInjectTask : IAuto, IActivityLifecycle, IApplicationLifecycle {
    override fun onApplicationCreated(app: Application) {
        Router.init(app)
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        Router.inject(activity)
        println("enter this line onActivityPreCreated")
    }


}

class Router(uri: Uri = Uri.EMPTY) {
    private var uriBuilder = uri.buildUpon()
    private val mBundle = bundleOf()

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
    inline fun <reified T> syncNavigation(context: Context? = null): T? {
        val resultRef = AtomicReference<T>()
        val flagRef = AtomicReference(0)
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val result = forward(context)
                resultRef.set(result as? T)
            }
            flagRef.set(1)
        }
        while (flagRef.get() == 0) {
            //do nothing,Thread loop
        }
        return resultRef.get()
    }

    suspend fun forward(context: Context? = null): Any? {
        return request(context).navigate()
    }


    suspend fun request(context: Context?): RouterResponse {
        return routerRequest(uriBuilder.build(), mBundle, context)
            .onInterceptors {
                val request = request()
                routerResponse(request.uri, request.bundle, request.contextHolder)
            }.beforeIntercept {
                request().syncUriToBundle()
            }.add(ActionDelegateRouterInterceptor())
            .start()
    }


    companion object Init : InitI {


        private fun ActivityActionDelegate.injectInternal(bundle: Bundle, activity: Activity) {
            inject(bundle, activity)
            var parentPath = parentPath()
            var action: ActivityActionDelegate?
            while (parentPath.isNotEmpty()) {
                action = ActionCenter.getAction(parentPath) as? ActivityActionDelegate
                if (action != null) {
                    parentPath = action.parentPath()
                    action.inject(bundle, activity)
                } else {
                    parentPath = ""
                }
            }
        }

        internal fun <T> inject(activity: T) where T : Activity {
            val intent = activity.intent ?: return
            val key = ParameterSupport.getCenterKey(intent) ?: return
            val center = ActionCenter.getAction(key)
            val bundle = intent.extras ?: return
            (center as? ActivityActionDelegate)?.apply {
                injectInternal(bundle, activity)
            }
        }


        internal var app by Delegates.notNull<Application>()
        private var hasInit = false

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
            return this
        }

        fun from(uri: Uri): Router {
            return Router(uri)
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





