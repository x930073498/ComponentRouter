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
import com.x930073498.component.router.thread.IThread
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
        val contextHolder = ContextHolder.create(context)
       return ActionCenter.getResultDirect(uri, bundle, contextHolder)
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
