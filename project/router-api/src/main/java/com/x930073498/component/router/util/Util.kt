package com.x930073498.component.router.util

import android.net.Uri
import com.x930073498.component.router.interceptor.ChainSource
import com.x930073498.component.router.request.RouterRequest

fun Uri.authorityAndPath(): Uri {
    return Uri.Builder()
        .path(path)
        .authority(authority)
        .build()
}
suspend fun <T> ChainSource<T>.targetChanged(): Boolean where T : RouterRequest {
    val oUri = headerRequest().uri.buildUpon().clearQuery().build()
    val uri = request().uri.buildUpon().clearQuery().build()
    return oUri == uri
}