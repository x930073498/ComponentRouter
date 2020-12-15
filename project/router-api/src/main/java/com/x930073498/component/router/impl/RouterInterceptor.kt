package com.x930073498.component.router.impl

import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.NavigateParams
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



class ActionDelegateRouterInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        val request = chain.request()
        val uri = request.uri
        val action = ActionCenter.getAction(uri)
        val interceptors = action.interceptors()
        return if (interceptors.isEmpty()) {
            chain.process(request)
        } else {
            interceptors.reversed().forEach {
                val interceptor = ActionCenter.getAction(it)
                    .getResult(NavigateParams(request.bundle,request.contextHolder)) as? RouterInterceptor
                if (interceptor != null) {
                    chain.addNext(interceptor)
                }
            }
            chain.process(request)
        }
    }

}