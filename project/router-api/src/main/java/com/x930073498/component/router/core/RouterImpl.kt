package com.x930073498.component.router.core

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.util.lruCache
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.LaunchMode
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.resultOf
import com.x930073498.component.router.coroutines.scopeResultOf
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.onInterceptors
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.routerResponse
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.CoroutineScope
import java.lang.RuntimeException
import java.util.*
import kotlin.coroutines.CoroutineContext


internal class RequestParams(
    val uri: Uri,
    val bundle: Bundle,
    val interceptors: List<String>,
    val isGreenChannel: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestParams

        if (uri != other.uri) return false

        return bundle.keySet().all {
            bundle[it] == other.bundle[it]
        }
    }



    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + Objects.hash(*bundle.keySet().map { it to bundle[it] }
            .toTypedArray())
        return result
    }
}

internal class RouterImpl private constructor(
    private val mHandler: InternalRouterHandler
) :
    IRouter(), IRouterHandler by mHandler {

    companion object {

        private val requestParams = lruCache<RequestParams, Long>(50)

        internal fun getParamsTime(params: RequestParams): Long {
            return requestParams[params] ?: 0
        }

        internal fun setParamsTime(params: RequestParams) {
            requestParams.put(params, System.currentTimeMillis())
        }
    }

   internal constructor(uri: Uri = Uri.EMPTY) : this(
        InternalRouterHandler(uri)
    )


    override suspend fun requestInternal(
        coroutineContext: CoroutineContext?,
        debounce: Long,
        context: Context?,
        request: suspend IRouterHandler.() -> Unit
    ): ResultListenable<RouterResponse> {
        return scopeResultOf(coroutineContext) {
            request(mHandler)
            RequestParams(
                mHandler.uriBuilder.build(),
                mHandler.mBundle,
                mHandler.interceptors,
                mHandler.greenChannel
            )
        }.toResponse(debounce, context)
    }

    override fun requestInternal(
        scope: CoroutineScope,
        coroutineContext: CoroutineContext,
        debounce: Long,
        context: Context?,
        request: suspend IRouterHandler.() -> Unit

    ): ResultListenable<RouterResponse> {
        return resultOf(scope, coroutineContext) {
            request(mHandler)
            RequestParams(
                mHandler.uriBuilder.build(),
                mHandler.mBundle,
                mHandler.interceptors,
                mHandler.greenChannel
            )
        }
            .toResponse(debounce, context)

    }

    override fun requestInternalDirect(
        debounce: Long,
        context: Context?,
        request: IRouterHandler.() -> Unit
    ): DirectRequestResult {
        if (!isMainThread) throw RuntimeException("请在主线程调用")
        request(mHandler)
        val bundle = mHandler.mBundle
        val uri = mHandler.uriBuilder.build()
        val params = RequestParams(uri, bundle, emptyList(), true)
        val time = getParamsTime(params)
        setParamsTime(params)
        ParameterSupport.syncUriToBundle(uri, bundle)
        if (System.currentTimeMillis() - time < debounce) {
            return DirectRequestResult.Ignore
        }
        val action = ActionCenter.getAction(uri)
        val contextHolder = ContextHolder.create(context)
        when (val target = action.target) {
            is Target.ServiceTarget -> {
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.ServiceResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.MethodTarget -> {
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.MethodResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.ActivityTarget -> {
                val actualContext = contextHolder.getContext()
                val intent = Intent(actualContext, target.targetClazz)
                val componentName = intent.resolveActivity(contextHolder.getPackageManager())
                if (componentName != null) {
                    intent.apply {
                        if (actualContext is Application) {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        } else {
                            when (target.action.launchMode()) {
                                LaunchMode.Standard -> {
                                    //doNothing
                                    LogUtil.log("enter this line Standard")
                                }
                                LaunchMode.SingleTop -> {
                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                }
                                LaunchMode.SingleTask -> {
                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                LaunchMode.NewTask -> {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                        }
                        putExtras(bundle)
                        actualContext.startActivity(intent)
                        return DirectRequestResult.ActivityResult(bundle, contextHolder)
                    }
                }
                return DirectRequestResult.Empty(bundle, contextHolder)
            }
            is Target.FragmentTarget -> {
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.FragmentResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.InterceptorTarget -> {
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz)
                target.action.inject(bundle, result)
                return DirectRequestResult.InterceptorResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.SystemTarget -> {
                val actualUri = ParameterSupport.getUriAsString(bundle)
                val actualContext = contextHolder.getContext()
                var intent = Intent.parseUri(actualUri, 0)
                var info = actualContext.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                if (info != null) {
                    if (info.activityInfo.packageName != actualContext.packageName) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    actualContext.startActivity(intent)
                    return DirectRequestResult.ActivityResult(bundle, contextHolder)
                }
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(actualUri)
                intent.putExtras(bundle)
                info = actualContext.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                with(info) {
                    if (this == null) {
                        LogUtil.log(
                            "没找到对应路径{'${
                                actualUri
                            }'}的组件,请检查路径以及拦截器的设置"
                        )
                        return DirectRequestResult.Empty(bundle, contextHolder)
                    } else {
                        if (activityInfo.packageName != actualContext.packageName) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        actualContext.startActivity(intent)
                        return DirectRequestResult.ActivityResult(bundle, contextHolder)
                    }
                }
            }
        }
    }


    private fun ResultListenable<RequestParams>.toResponse(
        debounce: Long,
        context: Context?,
    )
            : ResultListenable<RouterResponse> {
        return createUpon {
            val time = getParamsTime(it)
            setParamsTime(it)
            if (System.currentTimeMillis() - time > debounce) {
                setResult(
                    routerRequest(it.uri, it.bundle, context)
                        .onInterceptors {
                            val request = request()
                            routerResponse(
                                request.uri,
                                request.bundle,
                                request.contextHolder
                            )
                        }.beforeIntercept {
                            request().syncUriToBundle()
                        }
                        .apply {
                            if (!it.isGreenChannel) {
                                add(ActionDelegateRouterInterceptor())
                                add(PathRouterInterceptor(it.interceptors))
                                add(GlobalInterceptor)
                            }
                        }
                        .start())
            } else {
                dispose()
            }
        }
    }
}
