package com.x930073498.component.router.impl

import com.x930073498.component.router.Router
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.scopeNavigate
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.Interceptor
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.response.RouterResponse

interface RouterInterceptor :
    Interceptor<RouterRequest, RouterResponse, Chain<RouterRequest, RouterResponse>> {
    companion object {
        val Empty = object : RouterInterceptor {
            override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
                return chain.process(chain.request())
            }

        }
    }
}


class PathRouterInterceptor(private val paths: List<String>) : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        paths.forEach { url ->
            val interceptor =
                Router.from(url)
                    .scopeNavigate(debounce = -1)
                    .await()
                    .getResult()
            if (interceptor != null) {
                if (interceptor is RouterInterceptor)
                    chain.addNext(interceptor)
            }
        }
        return chain.process(chain.request())

    }
}

class ActionDelegateRouterInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        val request = chain.request()
        val uri = request.uri
        val action = ActionCenter.getAction(uri)
        val interceptors = action.interceptors()
        return if (interceptors.isEmpty()) {
            chain.process(request)
        } else {
            interceptors.reversed().forEach { url ->
                val interceptor =
                    Router.from(url)
                        .scopeNavigate(debounce = -1)
                        .await()
                        .getResult()
                if (interceptor != null) {
                    if (interceptor is RouterInterceptor)
                        chain.addNext(interceptor)
                }
            }
            chain.process(request)
        }
    }

}