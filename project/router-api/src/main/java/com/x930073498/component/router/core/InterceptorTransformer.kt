package com.x930073498.component.router.core

import com.x930073498.component.router.interceptor.Transformer
import com.x930073498.component.router.request.InternalRouterRequest
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.response.RouterResponse
import com.x930073498.component.router.response.routerResponse

internal class RequestToResponseTransformer : Transformer<RouterRequest, RouterResponse> {
    override fun transform(data: RouterRequest): RouterResponse {
        data.syncUriToBundle()
        return routerResponse(data.uri, data.bundle, data.contextHolder)
    }
}

internal class ResponseToRequestTransformer(private val header: RouterRequest) :
    Transformer<RouterResponse, RouterRequest> {
    override fun transform(data: RouterResponse): RouterRequest {
        return InternalRouterRequest(header, data.uri, data.bundle, data.contextHolder)
    }
}