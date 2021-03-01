package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.core.ISerializerBundle
import com.x930073498.component.router.core.createFormBundle

internal class InternalRouterRequestBuilder(private val request: RouterRequest) :
    RouterRequest.Builder {
    private var uri = request.uri
    private var bundle = Bundle(request.bundle)
    private val iBundle = createFormBundle(bundle)
    override fun uri(uri: Uri): RouterRequest.Builder {
        this.uri = uri
        return this
    }

    override fun uri(uriBuilder: Uri.Builder.(Uri) -> Unit): RouterRequest.Builder {
        val builder = uri.buildUpon()
        uriBuilder.invoke(builder, uri)
        uri = builder.build()
        return this
    }

    override fun bundle(bundle: Bundle): RouterRequest.Builder {
        this.bundle = bundle
        return this
    }

    override fun serializer(action: ISerializerBundle.() -> Unit): RouterRequest.Builder {
        action(iBundle)
        return this
    }

    override fun bundle(action: Bundle.() -> Unit): RouterRequest.Builder {
        action(bundle)
        return this
    }


    override fun build(): RouterRequest {
        return InternalRouterRequest(request.header, uri, bundle, request.contextHolder)
    }

}
