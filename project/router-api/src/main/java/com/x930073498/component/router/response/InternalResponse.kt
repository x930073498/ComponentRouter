package com.x930073498.component.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.action.ContextHolder

internal class InternalResponse internal constructor(
    uri: Uri,
    bundle: Bundle,
    contextHolder: ContextHolder,
    isSuccessful: Boolean = true,
) : RouterResponse {


    constructor(response: RouterResponse) : this(
        response.uri,
        response.bundle,
        response.contextHolder,
        response.isSuccessful
    )

    private var mUri = uri
    private var mBundle = bundle
    var mSuccess = isSuccessful
    private var mContextHolder = contextHolder


    override val uri: Uri
        get() = mUri
    override val bundle: Bundle
        get() = mBundle
    override val contextHolder: ContextHolder
        get() = mContextHolder
    override val isSuccessful: Boolean
        get() = mSuccess

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InternalResponse

        if (mUri != other.mUri) return false
        if (mBundle != other.mBundle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mUri.hashCode()
        result = 31 * result + mBundle.hashCode()
        return result
    }


}

fun routerResponse(uri: Uri, bundle: Bundle,contextHolder: ContextHolder): RouterResponse {
    return InternalResponse(uri, bundle,contextHolder)
}