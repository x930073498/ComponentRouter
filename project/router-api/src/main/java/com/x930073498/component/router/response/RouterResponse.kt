package com.x930073498.component.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.action.NavigateInterceptor
import com.x930073498.component.router.action.NavigateParams
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.ResultHandler
import com.x930073498.component.router.impl.getResult
import com.x930073498.component.router.interceptor.Response
import com.x930073498.component.router.util.ParameterSupport

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
internal fun RouterResponse.asNavigateParams():NavigateParams{
    return NavigateParams(bundle,contextHolder)
}


suspend fun RouterResponse.navigate(navigateInterceptor: NavigateInterceptor?=null,handler: ResultHandler= ResultHandler.Direct): Any? {
    return ActionCenter.getAction(uri).getResult(asNavigateParams(),navigateInterceptor,handler)
}
 fun RouterResponse.asActionDelegate():ActionDelegate{
    return ActionCenter.getAction(uri).apply {
        ParameterSupport.putCenter(bundle, path)
    }
}



