package com.x930073498.router.response

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.x930073498.router.interceptor.Response
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.navigate

interface RouterResponse : Response {
    val uri: Uri
    val bundle: Bundle
    val isSuccessful: Boolean//表示执行成功


    companion object {
        val Empty = object : RouterResponse {
            override val uri: Uri
                get() = Uri.EMPTY
            override val bundle: Bundle
                get() = Bundle.EMPTY
            override val isSuccessful: Boolean
                get() = true

        }
    }
}
fun RouterResponse.success(isSuccess: Boolean = true): RouterResponse {
    return InternalResponse(this).also { it.mSuccess = isSuccess }
}



@Suppress("UNCHECKED_CAST")
suspend fun <T> RouterResponse.navigate(context: Context? = null): T? {
    return ActionCenter.getAction(uri)?.navigate(bundle, ContextHolder.create(context)) as? T
}

@Suppress("UNCHECKED_CAST")
suspend fun RouterResponse.forward(context: Context? = null) {
    navigate<Any?>(context)
}

