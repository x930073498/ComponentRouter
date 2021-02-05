package com.x930073498.component.router.impl

import android.net.Uri
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.*
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.coroutines.result
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.Interceptor
import com.x930073498.component.router.navigator.interceptorNavigate
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.requestDirectAsService
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.scopeInterceptor

interface RouterInterceptor : Interceptor<RouterRequest, RouterResponse, Chain<RouterRequest, RouterResponse>> {
    companion object {
        val Empty = object : RouterInterceptor {
            override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
                return chain.process(chain.request())
            }

        }
    }
}

abstract class PathReplaceInterceptor : RouterInterceptor {
    final override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        val request = chain.request()
        val path = replace(request.uri.path)
        val uri = replace(request.uri.buildUpon().path(path).build())
        return chain.process( request.buildUpon().uri(uri).build())

    }

    open fun replace(path: String?): String? {
        return path
    }

    open fun replace(uri: Uri): Uri {
        return uri
    }

}


class PathRouterInterceptor(private val paths: List<String>) : RouterInterceptor {
    override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
        paths.forEach { url ->
            val interceptor =
                Router.from(url)
                    .requestDirectAsInterceptor()
                    ?.result
            if (interceptor != null)
                chain.addNext(interceptor)
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
            interceptors.forEach { url ->
                val interceptor =
                    Router.from(url)
                        .requestDirectAsInterceptor()
                        ?.result
                if (interceptor != null)
                    chain.addNext(interceptor)
            }
            chain.process(request)
        }
    }

}