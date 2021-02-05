package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.core.ISerializerBundle
import com.x930073498.component.router.interceptor.Request

interface RouterRequest : Request {
    val uri: Uri
    val bundle: Bundle
    suspend fun buildUpon(): Builder
    suspend fun syncUriToBundle()
    interface Builder {
        suspend fun uri(uri: Uri):Builder
        suspend fun bundle(bundle: Bundle):Builder
        suspend fun uri(uriBuilder: suspend Uri.Builder.(Uri) -> Unit): Builder
        suspend fun serializer(action: suspend ISerializerBundle.() -> Unit): Builder
        suspend fun bundle(action: Bundle.() -> Unit): Builder
        suspend fun build(): RouterRequest
    }

    companion object {
        suspend fun builder(): Builder {
            return Empty.buildUpon()
        }

        val Empty = object : RouterRequest {
            override val uri: Uri = Uri.EMPTY

            override val bundle: Bundle = bundleOf()


            override suspend fun buildUpon(): Builder {
                return InternalRouterRequestBuilder(this)
            }

            override suspend fun syncUriToBundle() {
            }


        }
    }
}


