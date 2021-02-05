package com.x930073498.component.router.core

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.util.lruCache
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.resultOf
import com.x930073498.component.router.coroutines.scopeResultOf
import com.x930073498.component.router.globalInterceptors
import com.x930073498.component.router.impl.InterceptorActionDelegate
import com.x930073498.component.router.impl.RouterInterceptor
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.onInterceptors
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.routerResponse
import com.x930073498.component.router.util.ParameterSupport
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext


internal class RequestParams(
    val uri: Uri,
    val bundle: Bundle,
    val interceptors: List<String>,
    val replaceInterceptors: List<String>,
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
        return resultOf(scope, coroutineContext) {
            request(mHandler)
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
    ): DirectRequestResult {
        request(mHandler)
        val bundle = mHandler.mBundle
        val uri = mHandler.uriBuilder.build()
        val params = RequestParams(uri, bundle, emptyList(), emptyList(), true)
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
        val contextHolder = ContextHolder.create(context)
        return createUpon { setter ->
            val time = getParamsTime(setter)
            setParamsTime(setter)
            if (System.currentTimeMillis() - time > debounce) {
                setResult(
                    routerRequest(setter.uri, setter.bundle)
                        .onInterceptors {
                            val request = request()
                            routerResponse(
                                request.uri,
                                request.bundle,
                                contextHolder
                            )
                        }.beforeIntercept {
                            request().syncUriToBundle()
                        }
                        .apply {
                            if (!setter.isGreenChannel) {
                                add(
                                    InternalInterceptor(
                                        setter.interceptors,
                                        setter.replaceInterceptors
                                    )
                                )
                            }
                        }
                        .start())
            } else {
                dispose()
            }
        }
    }
}

internal class InternalInterceptor internal constructor(
    private val handlerInterceptorList: List<String>,
    private val replaceInterceptors: List<String>
) :
    RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        val uri = chain.request().uri
        val action = ActionCenter.getAction(uri)
        val interceptors = globalInterceptors.sorted().toMutableList()
        val actionInterceptors =
            (if (replaceInterceptors.isNotEmpty()) replaceInterceptors else action.interceptors() + handlerInterceptorList).mapNotNull {
                ActionCenter.getAction(it) as? InterceptorActionDelegate
            }
        val min = actionInterceptors.minOrNull()
        if (min != null) {
            val index = interceptors.indexOfFirst {
                it > min
            }
            if (index >= 0) {
                interceptors.addAll(index, actionInterceptors)
            } else {
                interceptors.addAll(0, actionInterceptors)
            }
        }
        interceptors.reversed().forEach {
            chain.addNext(it.factory().create(ContextHolder.create(), it.target.targetClazz))
        }
        return chain.process(chain.request())
    }

}
