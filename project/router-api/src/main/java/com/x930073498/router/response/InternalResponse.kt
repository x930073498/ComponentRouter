package com.x930073498.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.ActionDelegate

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


}

fun routerResponse(uri: Uri, bundle: Bundle,contextHolder: ContextHolder): RouterResponse {
    return InternalResponse(uri, bundle,contextHolder)
}