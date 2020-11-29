@file:Suppress("UNCHECKED_CAST")

package com.x930073498.router

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.x930073498.router.interceptor.*
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.impl.*
import com.x930073498.router.request.routerRequest
import com.x930073498.router.response.RouterResponse
import com.x930073498.router.response.forward
import com.x930073498.router.response.navigate
import com.x930073498.router.response.routerResponse
import com.x930073498.router.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


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
        return withContext(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                request()
            }.navigate(context)
        }
    }

    @ExperimentalStdlibApi
    fun <T> syncNavigation(context: Context? = null): T? {
        return runBlocking {
            runCatching { navigate<T>(context) }
                .onFailure { it.printStackTrace() }
                .getOrNull()
        }
    }

    suspend fun forward(context: Context? = null) {
        return withContext(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                request()
            }.forward(context)
        }
    }

    suspend fun request(): RouterResponse {
        return routerRequest(uriBuilder.build(), mBundle)
            .onInterceptors {
                routerResponse(request().uri, request().bundle)
            }.beforeIntercept {
                request().syncUriToBundle()
            }
            .start()
    }


    internal object RouterActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks,
        FragmentManager.FragmentLifecycleCallbacks() {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            inject(activity)
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(this, true)
            }
        }


        override fun onFragmentPreCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?,
        ) {
            super.onFragmentPreCreated(fm, f, savedInstanceState)
            inject(f)
        }


        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(this)
            }

        }

    }


    companion object Init : InitI {

        internal fun <T> inject(activity: T) where T : Activity {
            val intent = activity.intent ?: return
            val key = ParameterSupport.getCenterKey(intent) ?: return
            val center = ActionCenter.getAction(key)
            (center as? ActivityActionDelegate<T>)?.inject(intent, activity)
        }

        internal fun <T> inject(fragment: T) where T : Fragment {
            val bundle = fragment.arguments ?: return
            val key = ParameterSupport.getCenterKey(bundle) ?: return
            val center = ActionCenter.getAction(key)
            (center as? FragmentActionDelegate<T>)?.inject(bundle, fragment)
        }

        internal suspend fun <T> inject(provider: T, bundle: Bundle) where T : IService {
            val key = ParameterSupport.getCenterKey(bundle) ?: return
            val center = ActionCenter.getAction(key)
            (center as? ServiceActionDelegate<T>)?.inject(bundle, provider)
        }

        internal var app by Delegates.notNull<Application>()
        private var hasInit = false

        @Synchronized
        override fun init(app: Application): InitI {
            if (hasInit) return this
            this.app = app
            app.registerActivityLifecycleCallbacks(RouterActivityLifecycleCallbacks)
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





