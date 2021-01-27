package com.x930073498.component.router.core

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.util.lruCache
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.resultOf
import com.x930073498.component.router.coroutines.scopeResultOf
import com.x930073498.component.router.impl.ActionDelegateRouterInterceptor
import com.x930073498.component.router.interceptor.onInterceptors
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.routerResponse
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class RouterImpl private constructor(private val mHandler: InternalRouterHandler) :
    IRouter(), IRouterHandler by mHandler {

    companion object {
        private class RequestParams(val uri: Uri, val bundle: Bundle) {
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

        private val requestParams = lruCache<RequestParams, Long>(50)

        private fun getParamsTime(params: RequestParams): Long {
            return requestParams[params] ?: 0
        }

        private fun setParamsTime(params: RequestParams) {
            requestParams.put(params, System.currentTimeMillis())
        }
    }

    constructor(uri: Uri = Uri.EMPTY) : this(InternalRouterHandler(uri))


    override suspend fun requestInternal(
        coroutineContext: CoroutineContext?,
        debounce: Long,
        context: Context?
    ): ResultListenable<RouterResponse> {
        return scopeResultOf(coroutineContext) {
            RequestParams(mHandler.uriBuilder.build(), mHandler.mBundle)
        }.createUpon {
            val time = getParamsTime(it)
            setParamsTime(it)
            if (System.currentTimeMillis() - time > debounce) {
                setResult(
                    routerRequest(it.uri, it.bundle, context)
                        .onInterceptors {
                            val request = request()
                            routerResponse(request.uri, request.bundle, request.contextHolder)
                        }.beforeIntercept {
                            request().syncUriToBundle()
                        }.add(ActionDelegateRouterInterceptor())
                        .apply {
                            if (!mHandler.greenChannel) add(GlobalInterceptor)
                        }
                        .start())
            } else {
                dispose()
            }
        }
    }

    override fun requestInternal(
        scope: CoroutineScope,
        coroutineContext: CoroutineContext,
        debounce: Long,
        context: Context?
    ): ResultListenable<RouterResponse> {
        return resultOf(scope, coroutineContext) {
            RequestParams(mHandler.uriBuilder.build(), mHandler.mBundle)
        }.createUpon {
            val time = getParamsTime(it)
            setParamsTime(it)
            if (System.currentTimeMillis() - time > debounce) {
                setResult(
                    routerRequest(it.uri, it.bundle, context)
                        .onInterceptors {
                            val request = request()
                            routerResponse(request.uri, request.bundle, request.contextHolder)
                        }.beforeIntercept {
                            request().syncUriToBundle()
                        }.add(ActionDelegateRouterInterceptor())
                        .apply {
                            if (!mHandler.greenChannel) add(GlobalInterceptor)
                        }
                        .start())
            } else {
                dispose()
            }
        }
    }


}