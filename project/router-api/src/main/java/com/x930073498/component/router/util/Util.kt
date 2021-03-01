package com.x930073498.component.router.util

import android.net.Uri

fun Uri.authorityAndPath(): Uri {
    return Uri.Builder()
        .path(path)
        .authority(authority)
        .build()
}
