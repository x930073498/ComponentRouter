package com.x930073498.router.response

import android.net.Uri
import android.os.Bundle

internal class InternalResponse internal constructor(
    uri: Uri,
    bundle: Bundle,
    isSuccessful: Boolean = true,
) : RouterResponse {


    constructor(response: RouterResponse) : this(response.uri,
        response.bundle,
        response.isSuccessful)

    private var mUri = uri
    private var mBundle = bundle
    var mSuccess = isSuccessful

    override val uri: Uri
        get() = mUri
    override val bundle: Bundle
        get() = mBundle
    override val isSuccessful: Boolean
        get() = mSuccess

}
fun routerResponse(uri: Uri, bundle: Bundle): RouterResponse {
    return InternalResponse(uri, bundle)
}