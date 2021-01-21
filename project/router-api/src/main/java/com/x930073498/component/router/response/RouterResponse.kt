package com.x930073498.component.router.response

import android.net.Uri
import android.os.Bundle
import com.x930073498.component.router.action.*
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.impl.*
import com.x930073498.component.router.interceptor.Response
import com.x930073498.component.router.navigator.*
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




fun ResultListenable<RouterResponse>.asNavigator(navigatorOption: NavigatorOption=NavigatorOption.Empty):DispatcherNavigator {
    return DispatcherNavigator(this,navigatorOption)
}


fun RouterResponse.asActionDelegate(): ActionDelegate {
    return ActionCenter.getAction(uri).apply {
        ParameterSupport.putCenter(bundle, path)
    }
}




