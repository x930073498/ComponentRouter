package com.x930073498.router.request

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.x930073498.router.util.ParameterSupport

internal class InternalRouterRequest private constructor() : RouterRequest, Parcelable {
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

    constructor(parcel: Parcel) : this() {
        mUri = parcel.readParcelable(Uri::class.java.classLoader)
        mBundle = parcel.readBundle(Bundle::class.java.classLoader) ?: bundleOf()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(mUri, flags)
        parcel.writeBundle(mBundle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InternalRouterRequest> {
        val KEY_SYNC_URI = "_componentSyncUri"
        const val KEY_URI_QUERY_BUNDLE = "_componentQueryBundle"
        const val KEY_URI = "_componentRouterUri"

        override fun createFromParcel(parcel: Parcel): InternalRouterRequest {
            return InternalRouterRequest(parcel)
        }

        override fun newArray(size: Int): Array<InternalRouterRequest?> {
            return arrayOfNulls(size)
        }
    }

}
fun routerRequest(uri: Uri, bundle: Bundle = bundleOf()): RouterRequest {
    return InternalRouterRequest(uri, bundle)
}