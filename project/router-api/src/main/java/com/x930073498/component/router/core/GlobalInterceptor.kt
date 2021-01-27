package com.x930073498.component.router.core

import com.x930073498.component.router.Router.from
import com.x930073498.component.router.globalInterceptors
import com.x930073498.component.router.impl.InterceptorActionDelegate
import com.x930073498.component.router.impl.RouterInterceptor
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.scopeNavigate

internal object GlobalInterceptor : RouterInterceptor {
        override suspend fun intercept(chain: Chain<RouterRequest, RouterResponse>): RouterResponse {
            val request = chain.request()
            globalInterceptors.reversed().mapNotNull {
                when (it) {
                    is String -> from(it).scopeNavigate().await()
                        .getResult() as? RouterInterceptor
                    is RouterInterceptor -> it
                    is InterceptorActionDelegate -> it.factory()
                        .create(request.contextHolder, it.target.targetClazz)
                    else -> null
                }
            }.forEach { chain.addNext(it) }
            return chain.process(request)
        }

    }