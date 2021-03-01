package com.x930073498.component.router.core

import android.net.Uri
import android.os.Bundle
import java.util.*

internal class RequestParams(
    val uri: Uri,
    val bundle: Bundle,
    val interceptors: List<String>,
    val replaceInterceptors: List<String>,
    val isGreenChannel: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestParams

        if (uri != other.uri) return false

        return bundle.keySet().all {
            bundle[it] == other.bundle[it]
        }
    }


    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + Objects.hash(*bundle.keySet().map { it to bundle[it] }
            .toTypedArray())
        return result
    }
}
