package com.x930073498.router.request

import android.net.Uri
import android.os.Bundle

internal class InternalRouterRequestBuilder(request: RouterRequest) : RouterRequest.Builder {
    private var uri = request.uri
    private var bundle = Bundle(request.bundle)
    override suspend fun uri(uriBuilder: suspend Uri.Builder.(Uri) -> Unit): RouterRequest.Builder {
        val builder = uri.buildUpon()
        uriBuilder.invoke(builder, uri)
        uri = builder.build()
        return this
    }

    override suspend fun bundle(bundleBuilder: suspend Bundle.() -> Unit): RouterRequest.Builder {
        bundleBuilder(bundle)
        return this
    }

    override suspend fun build(): RouterRequest {
        return InternalRouterRequest(uri, bundle)
    }

}
