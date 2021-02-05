package com.x930073498.component.test

import com.x930073498.component.annotations.InterceptorAnnotation
import com.x930073498.component.annotations.InterceptorScope
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.impl.RouterInterceptor
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.response.RouterResponse

@InterceptorAnnotation(
    "/test/interceptors/test1",
    scope = InterceptorScope.NORMAL
)
class TestInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        LogUtil.log("enter this line test1")
        return chain.process(chain.request())
    }
}

@InterceptorAnnotation(
    "/test/interceptors/test2",
    scope = InterceptorScope.NORMAL
)
class Test2Interceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        LogUtil.log("enter this line test2")
        return chain.process(chain.request())
    }
}
@InterceptorAnnotation(
    "/test/interceptors/test3",
    scope = InterceptorScope.NORMAL
)
class Test3Interceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        LogUtil.log("enter this line test3")
        return chain.process(chain.request())
    }
}

@InterceptorAnnotation(
    "/test/interceptors/testGlobal1",
    scope = InterceptorScope.GLOBAL,
    priority = 0
)
class TestGlobal2Interceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        LogUtil.log("enter this line testGlobal1")
        return chain.process(chain.request())
    }
}
@InterceptorAnnotation(
    "/test/interceptors/testGlobal2",
    scope = InterceptorScope.GLOBAL,
    priority =1000
)
class TestGlobal1Interceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        LogUtil.log("enter this line testGlobal2")
        return chain.process(chain.request())
    }
}

@InterceptorAnnotation(
    path = "/interceptor/scheme-http",
    scope = InterceptorScope.GLOBAL
)
class HttpSchemeInterceptor : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        val request = chain.request()
        val uri = request.uri
        val scheme = uri.scheme
        return if (scheme.isNullOrEmpty()) {
            chain.process(request)
        } else {
            if (scheme == "http" || scheme == "https") {
                chain.process(request.buildUpon()
                    .uri {
                        path("/fragment/web")
                    }
                    .serializer {
                        put("url", uri.toString())
                    }
                    .build())
            } else {
                chain.process(request)
            }
        }
    }

}