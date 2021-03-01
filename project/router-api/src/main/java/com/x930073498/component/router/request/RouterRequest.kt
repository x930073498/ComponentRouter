package com.x930073498.component.router.request

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.core.ISerializerBundle

interface RouterRequest {
    val uri: Uri
    val bundle: Bundle
    val contextHolder: ContextHolder
    fun buildUpon(): Builder
    fun syncUriToBundle()
    val header:RouterRequest
    interface Builder {
        fun uri(uri: Uri): Builder
        fun bundle(bundle: Bundle): Builder
        fun uri(uriBuilder: Uri.Builder.(Uri) -> Unit): Builder
        fun serializer(action: ISerializerBundle.() -> Unit): Builder
        fun bundle(action: Bundle.() -> Unit): Builder
        fun build(): RouterRequest
    }

    companion object {
        fun builder(): Builder {
            return Empty.buildUpon()
        }

        val Empty = object : RouterRequest {
            override val uri: Uri = Uri.EMPTY

            override val bundle: Bundle = bundleOf()
            override val contextHolder: ContextHolder
                get() = ContextHolder.create()


            override fun buildUpon(): Builder {
                return InternalRouterRequestBuilder(this)
            }

            override fun syncUriToBundle() {
            }

            override val header: RouterRequest
                get() = this


        }
    }
}


