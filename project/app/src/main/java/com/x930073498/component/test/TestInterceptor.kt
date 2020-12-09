package com.x930073498.component.test

import com.x930073498.annotations.InterceptorAnnotation
import com.x930073498.router.impl.RouterInterceptor
import com.x930073498.router.interceptor.Chain
import com.x930073498.router.request.RouterRequest
import com.x930073498.router.response.RouterResponse

@InterceptorAnnotation("/test/interceptors/test1")
class TestInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        println("enter this line interceptor test")
        val request = chain.request()
        return chain.process(request.buildUpon().uri {
            path("/test/method/test1")
        }.build())
    }
}
