package com.x930073498.component.router.core

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.util.lruCache
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.listenOf
import com.x930073498.component.router.interceptor.DisposeException
import com.x930073498.component.router.response.RouterResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


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

    internal constructor(uri: Uri = Uri.EMPTY,bundle: Bundle= bundleOf()) : this(
        InternalRouterHandler(uri,bundle)
    )


    override suspend fun requestInternal(
        coroutineContext: CoroutineContext?,
        debounce: Long,
        context: Context?,
        request: suspend IRouterHandler.() -> Unit
    ): ResultListenable<RouterResponse> {
        return listenOf(CoroutineScope(coroutineContext ?: EmptyCoroutineContext),coroutineContext?:EmptyCoroutineContext) {
            request(this)
            RequestParams(
                mHandler.uriBuilder.build(),
                mHandler.mBundle,
                mHandler.interceptors,
                mHandler.replaceInterceptors,
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
        return listenOf(scope,coroutineContext) {
            request(this)
            RequestParams(
                mHandler.uriBuilder.build(),
                mHandler.mBundle,
                mHandler.interceptors,
                mHandler.replaceInterceptors,
                mHandler.greenChannel
            )
        }
            .toResponse(debounce, context)

    }

    override fun requestInternalDirect(
        debounce: Long,
        context: Context?,
        request: IRouterHandler.() -> Unit
    ): RouterResponse {
        request(this)
        val params = RequestParams(
            mHandler.uriBuilder.build(),
            mHandler.mBundle,
            mHandler.interceptors,
            mHandler.replaceInterceptors,
            mHandler.greenChannel
        )
        val time = getParamsTime(params)
        setParamsTime(params)
        if (System.currentTimeMillis() - time < debounce) {
            return RouterResponse.Empty
        }
       return params.toResponse(context)

    }
}





