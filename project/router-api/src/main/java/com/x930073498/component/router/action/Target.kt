package com.x930073498.component.router.action

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.annotation.CallSuper
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.Router
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.Interceptor
import com.x930073498.component.router.thread.IThread
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

@Suppress("UNCHECKED_CAST")
sealed class Target(
    val targetClazz: Class<*>
) {
    @CallSuper
    open suspend fun go(params: NavigateParams): NavigateResult {
        if (!Router.hasInit) {
            throw RuntimeException("Router 尚未初始化成功")
        }
        return NavigateResult.empty
    }

    companion object {
        private val providerMap = mutableMapOf<Class<*>, Any?>()
        private val methodMap = mutableMapOf<Class<*>, Any?>()
        internal fun putProviderSingleton(clazz: Class<*>, any: Any?) {
            providerMap[clazz] = any
        }

        internal fun <T> getProviderSingleton(clazz: Class<T>): T? {
            return providerMap[clazz] as? T
        }

        internal fun putMethod(clazz: Class<*>, any: Any?) {
            methodMap[clazz] = any

        }

        internal fun getMethod(clazz: Class<*>): Any? {
            return methodMap[clazz]
        }


    }

    class ServiceTarget(
        targetClazz: Class<*>,
        private val isSingleTon: Boolean,
        val action: ServiceActionDelegate
    ) :
        Target(targetClazz) {
        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            val (bundle, contextHolder) = params
            return action.run {
                val factory = factory()
                val result = if (isSingleTon) {
                    getProviderSingleton(targetClazz) ?: run {
                        factory.create(contextHolder, targetClazz, bundle).also {
                            putProviderSingleton(targetClazz, it)
                        }
                    }
                } else {
                    factory.create(contextHolder, targetClazz, bundle)
                }
                if (result is IService) {
                    result.init(contextHolder, bundle)
                    inject(bundle, result)
                    if (autoInvoke())
                        result.invoke()
                }
                result
            }.asResult()
        }
    }

    protected suspend fun runOnThread(thread: IThread, action: suspend () -> Any?): Any? {
        return when (thread) {
            IThread.UI -> {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    action()
                } else {
                    withContext(Dispatchers.Main.immediate) {
                        action()
                    }
                }
            }
            IThread.WORKER -> {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    withContext(Dispatchers.IO) {
                        action
                    }
                } else {
                    action()
                }
            }
            IThread.ANY -> action()
        }
    }

    class MethodTarget(targetClazz: Class<*>, val action: MethodActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            val (bundle, contextHolder) = params
            return action.run {
                var invoker = getMethod(targetClazz)
                if (invoker == null) {
                    val factory = factory()
                    invoker = factory.create(contextHolder, targetClazz, bundle)
                    putMethod(targetClazz, invoker)
                }
                if (invoker is MethodInvoker) {
                    runOnThread(thread) {
                        invoker.invoke(contextHolder, bundle)
                    }
                } else null
            }.asResult()
        }

    }


    class ActivityTarget(targetClazz: Class<*>, val action: ActivityActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            val (bundle, contextHolder) = params
            return action.run {
                val context = contextHolder.getContext()
                val intent = Intent(context, targetClazz)
                if (context is Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.putExtras(bundle)
                context.startActivity(intent)
                NavigateResult.empty
            }
        }

    }

    class FragmentTarget(targetClazz: Class<*>, val action: FragmentActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            val (bundle, contextHolder) = params
            return action.run {
                if (Looper.getMainLooper() == Looper.getMainLooper()) {
                    factory().create(contextHolder, targetClazz, bundle).apply {
                        inject(bundle, this)
                    }
                } else {
                    withContext(Dispatchers.Main.immediate) {
                        factory().create(contextHolder, targetClazz, bundle)
                            .apply {
                                inject(bundle, this)
                            }
                    }
                }
            }.asResult()
        }
    }

    class InterceptorTarget(targetClazz: Class<*>, val action: InterceptorActionDelegate) :
        Target(targetClazz) {
        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            val (_, contextHolder) = params
            return action.run {
                factory().create(contextHolder, targetClazz)
            }.asResult()
        }
    }

    internal class SystemTarget :
        Target(Unit::class.java), NavigateInterceptor {
        private var navigateInterceptorRef = WeakReference<NavigateInterceptor>(null)
        internal fun setNavigateInterceptor(navigateInterceptor: NavigateInterceptor? = null) {
            navigateInterceptorRef = WeakReference(navigateInterceptor)
        }

        private fun go(context: Context, bundle: Bundle, uri: String?) {
            var intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
            var info = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (info != null) {
                if (info.activityInfo.packageName != context.packageName) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(uri)
            intent.putExtras(bundle)
            runCatching {
                info = context.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                with(info) {
                    if (this == null) {
                        LogUtil.log("没找到对应路径{'${ParameterSupport.getUriAsString(bundle)}'}的组件,请检查路径以及拦截器的设置")
                    } else {
                        if (activityInfo.packageName != context.packageName) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }.onFailure { it.printStackTrace() }
        }

        override suspend fun go(params: NavigateParams): NavigateResult {
            super.go(params)
            return toInterceptors(params)
                .apply {
                    Router.navigateInterceptor?.let {
                        add(it)
                    }
                    navigateInterceptorRef.get()?.let {
                        add(it)
                    }
                }
                .add(object : NavigateInterceptor {
                    override suspend fun intercept(chain: NavigateChain): NavigateResult {
                        val request = chain.request()
                        return if (request == params) chain.process(request)
                        else request.navigate()
                    }
                })
                .add(this)
                .start()
        }

        override suspend fun intercept(chain: NavigateChain): NavigateResult {
            val (bundle, contextHolder) = chain.request()
            return go(
                contextHolder.getContext(),
                bundle,
                ParameterSupport.getUriAsString(bundle)
            ).asResult()

        }
    }
}






