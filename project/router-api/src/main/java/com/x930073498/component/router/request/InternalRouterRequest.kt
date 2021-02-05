package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.router.util.ParameterSupport

internal class InternalRouterRequest private constructor() : RouterRequest{


    constructor(uri: Uri, bundle: Bundle) : this() {
        setBundle(bundle)
        setUri(uri)
    }

    private var mUri = Uri.EMPTY
    private var mBundle = bundleOf()

    private fun setUri(uri: Uri) {
        this.mUri = uri
    }

    private fun setBundle(bundle: Bundle) {
        mBundle.clear()
        mBundle.putAll(bundle)
    }


    override val uri: Uri
        get() = mUri
    override val bundle: Bundle
        get() = mBundle


    override suspend fun buildUpon(): RouterRequest.Builder {
        return InternalRouterRequestBuilder(this)
    }

    override suspend fun syncUriToBundle() {
        if (bundle.getInt(KEY_SYNC_URI) == uri.hashCode()) {
            return
        }
        ParameterSupport.syncUriToBundle(uri, bundle)
        bundle.putInt(KEY_SYNC_URI, uri.hashCode())
    }



    companion object   {
        const val KEY_SYNC_URI = "_componentSyncUri"
        const val KEY_URI_QUERY_BUNDLE = "_componentQueryBundle"
        const val KEY_URI = "_componentRouterUri"
    }

}

fun routerRequest(uri: Uri, bundle: Bundle = bundleOf()): RouterRequest {
    return InternalRouterRequest(uri, bundle)
}