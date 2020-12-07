package com.x930073498.router.request

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.RouterInterceptor
import com.x930073498.router.interceptor.Chain
import com.x930073498.router.interceptor.Request
import com.x930073498.router.response.RouterResponse

interface RouterRequest : Request {
    val uri: Uri
    val bundle: Bundle
    val contextHolder:ContextHolder
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
            override val contextHolder: ContextHolder
                get() = ContextHolder.create()


            override suspend fun buildUpon(): Builder {
                return InternalRouterRequestBuilder(this)
            }

            override suspend fun syncUriToBundle() {
            }



        }
    }
}


