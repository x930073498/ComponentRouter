package com.x930073498.router.request

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.x930073498.router.interceptor.Request

interface RouterRequest : Request, Parcelable {
    val uri: Uri
    val bundle: Bundle
    suspend fun buildUpon(): Builder
    suspend fun syncUriToBundle()
    interface Builder {
        suspend fun uri(uriBuilder: suspend Uri.Builder.(Uri) -> Unit): Builder
        suspend fun bundle(bundleBuilder: suspend Bundle.() -> Unit): Builder
        suspend fun build(): RouterRequest
    }

    companion object {
        val Empty = object : RouterRequest {
            override val uri: Uri = Uri.EMPTY

            override val bundle: Bundle = bundleOf()


            override suspend fun buildUpon(): Builder {
                return InternalRouterRequestBuilder(this)
            }

            override suspend fun syncUriToBundle() {
            }

            override fun describeContents(): Int {
                return 0
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {

            }

        }
    }
}