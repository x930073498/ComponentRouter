package com.x930073498.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.router.action.ActionCenter
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.navigate
import com.x930073498.router.interceptor.Response

interface RouterResponse : Response {
    val uri: Uri
    val bundle: Bundle
    val contextHolder: ContextHolder
    val isSuccessful: Boolean//表示执行成功


    companion object {
        val Empty = object : RouterResponse {
            override val uri: Uri
                get() = Uri.EMPTY
            override val bundle: Bundle
                get() = Bundle.EMPTY
            override val contextHolder: ContextHolder
                get() = ContextHolder.create()
            override val isSuccessful: Boolean
                get() = true

        }
    }
}

fun RouterResponse.success(isSuccess: Boolean = true): RouterResponse {
    return InternalResponse(this).also { it.mSuccess = isSuccess }
}


suspend fun RouterResponse.navigate(): Any? {
    return ActionCenter.getAction(uri).navigate(bundle, contextHolder)
}



