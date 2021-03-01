package com.x930073498.component.router.core

import android.content.Context
import com.x930073498.component.annotations.InterceptorAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.globalInterceptors
import com.x930073498.component.router.impl.CoroutineRouterInterceptor
import com.x930073498.component.router.impl.DirectRouterInterceptor
import com.x930073498.component.router.impl.InterceptorActionDelegate
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.DisposeException
import com.x930073498.component.router.interceptor.TransformerInterceptors
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.request.routerRequest
import com.x930073498.component.router.response.RouterResponse

internal fun ResultListenable<RequestParams>.toResponse(
    debounce: Long,
    context: Context?,
)
        : ResultListenable<RouterResponse> {
    val contextHolder = ContextHolder.create(context)
    return createUpon { setter ->
        val time = RouterImpl.getParamsTime(setter)
        RouterImpl.setParamsTime(setter)
        if (System.currentTimeMillis() - time > debounce) {
            val header = routerRequest(
                setter.uri,
                setter.bundle,
                contextHolder
            )
            val mInterceptors = TransformerInterceptors(
                RequestToResponseTransformer(),
                ResponseToRequestTransformer(header)
            )
            if (!setter.isGreenChannel) {
                mInterceptors.addInterceptor(
                    CoroutinePreGlobalInterceptor(),
                    CoroutineActionInterceptor(
                        setter.interceptors,
                        setter.replaceInterceptors
                    ),
                    CoroutinePostGlobalInterceptor()
                )
            }
            val result = runCatching {
                mInterceptors.requestCoroutine(header)
            }.getOrElse {
                if (it !is DisposeException) it.printStackTrace()
                else {
                    LogUtil.log("用户取消路由请求")
                }
                RouterResponse.Empty
            }
            if (result === RouterResponse.Empty) {
                dispose()
            } else
                setResult(result)
        } else {
            dispose()
        }
    }
}

internal class CoroutineActionInterceptor internal constructor(
    private val handlerInterceptorList: List<String>,
    private val replaceInterceptors: List<String>
) : CoroutineRouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest>): Chain.ChainResult<RouterRequest> {
        val uri = chain.request().uri
        val action = ActionCenter.getAction(uri)
        val actionInterceptors =
            (if (replaceInterceptors.isNotEmpty()) replaceInterceptors else action.interceptors() + handlerInterceptorList).mapNotNull {
                ActionCenter.getAction(it) as? InterceptorActionDelegate
            }.reversed()
        actionInterceptors.forEach {
            val interceptor = it.factory().create(ContextHolder.create(), it.target.targetClazz)
            if (interceptor is DirectRouterInterceptor)
                chain.addNext(interceptor)
            else if (interceptor is CoroutineRouterInterceptor) {
                chain.addNext(interceptor)
            }
        }
        return chain.process(chain.request())
    }

}

internal class CoroutinePostGlobalInterceptor internal constructor() : CoroutineRouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest>): Chain.ChainResult<RouterRequest> {
        var proGlobalInterceptors =
            globalInterceptors.filter { it.priority >= InterceptorAnnotation.DEFAULT_PRIORITY }
        proGlobalInterceptors = proGlobalInterceptors.sorted().reversed()
        proGlobalInterceptors.forEach {
            val interceptor = it.factory().create(ContextHolder.create(), it.target.targetClazz)
            if (interceptor is DirectRouterInterceptor)
                chain.addNext(interceptor)
            else if (interceptor is CoroutineRouterInterceptor) {
                chain.addNext(interceptor)
            }
        }
        return chain.process(chain.request())
    }
}


internal class CoroutinePreGlobalInterceptor internal constructor() : CoroutineRouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest>): Chain.ChainResult<RouterRequest> {
        var preGlobalInterceptors =
            globalInterceptors.filter { it.priority < InterceptorAnnotation.DEFAULT_PRIORITY }
        preGlobalInterceptors = preGlobalInterceptors.sorted().reversed()
        preGlobalInterceptors.forEach {
            val interceptor = it.factory().create(ContextHolder.create(), it.target.targetClazz)
            if (interceptor is DirectRouterInterceptor) {
                chain.addNext(interceptor)
            } else if (interceptor is CoroutineRouterInterceptor) {
                chain.addNext(interceptor)
            }
        }
        return chain.process(chain.request())
    }
}
