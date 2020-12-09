package com.x930073498.router.request

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.util.ParameterSupport

internal class InternalRouterRequest private constructor(context: Context?) : RouterRequest{
    private val _contextHolder:ContextHolder = ContextHolder.create(context)

    constructor(uri: Uri, bundle: Bundle, context: Context? = null) : this(context) {
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
    override val contextHolder: ContextHolder
        get() = _contextHolder

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

fun routerRequest(uri: Uri, bundle: Bundle = bundleOf(),context: Context?=null): RouterRequest {
    return InternalRouterRequest(uri, bundle,context)
}