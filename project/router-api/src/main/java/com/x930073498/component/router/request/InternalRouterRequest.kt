package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.util.ParameterSupport

internal class InternalRouterRequest private constructor() : RouterRequest {

    private var _header :RouterRequest= this

    constructor(
        header: RouterRequest? = null,
        uri: Uri,
        bundle: Bundle,
        contextHolder: ContextHolder
    ) : this() {
        setBundle(bundle)
        setUri(uri)
        setContextHolder(contextHolder)
        if (header != null)
            this._header = header
    }

    private var mUri = Uri.EMPTY
    private var mBundle = bundleOf()
    private var mContextHolder = ContextHolder.create()

    private fun setUri(uri: Uri) {
        this.mUri = uri
    }

    private fun setContextHolder(contextHolder: ContextHolder) {
        this.mContextHolder = contextHolder
    }

    private fun setBundle(bundle: Bundle) {
        mBundle.clear()
        mBundle.putAll(bundle)
    }


    override val uri: Uri
        get() = mUri
    override val bundle: Bundle
        get() = mBundle
    override val contextHolder: ContextHolder
        get() = mContextHolder


    override fun buildUpon(): RouterRequest.Builder {
        return InternalRouterRequestBuilder(this)
    }

    override fun syncUriToBundle() {
        if (bundle.getInt(KEY_SYNC_URI) == uri.hashCode()) {
            return
        }
        ParameterSupport.syncUriToBundle(uri, bundle)
        bundle.putInt(KEY_SYNC_URI, uri.hashCode())
    }

    override val header: RouterRequest
        get() = _header


    companion object {
        const val KEY_SYNC_URI = "_componentSyncUri"
        const val KEY_URI_QUERY_BUNDLE = "_componentQueryBundle"
        const val KEY_URI = "_componentRouterUri"
    }

}

internal fun routerRequest(
    uri: Uri,
    bundle: Bundle = bundleOf(),
    contextHolder: ContextHolder = ContextHolder.create()
): RouterRequest {
    return InternalRouterRequest(null,uri, bundle, contextHolder)
}