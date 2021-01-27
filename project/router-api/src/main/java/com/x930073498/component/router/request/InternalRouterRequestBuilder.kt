package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.core.ISerializerBundle
import com.x930073498.component.router.core.createFormBundle

internal class InternalRouterRequestBuilder(request: RouterRequest) : RouterRequest.Builder {
    private var uri = request.uri
    private var bundle = Bundle(request.bundle)
    private val iBundle = createFormBundle(bundle)

    override suspend fun uri(uriBuilder: suspend Uri.Builder.(Uri) -> Unit): RouterRequest.Builder {
        val builder = uri.buildUpon()
        uriBuilder.invoke(builder, uri)
        uri = builder.build()
        return this
    }

    override suspend fun serializer(action: suspend ISerializerBundle.() -> Unit): RouterRequest.Builder {
        action(iBundle)
        return this
    }

    override suspend fun bundle(action: Bundle.() -> Unit): RouterRequest.Builder {
        action(bundle)
        return this
    }


    override suspend fun build(): RouterRequest {
        return InternalRouterRequest(uri, bundle)
    }

}
