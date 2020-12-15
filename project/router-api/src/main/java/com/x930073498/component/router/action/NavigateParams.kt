package com.x930073498.component.router.action

import android.os.Bundle
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.navigate
import com.x930073498.component.router.interceptor.Request
import com.x930073498.component.router.interceptor.Response
import com.x930073498.component.router.util.ParameterSupport

data class NavigateParams(val bundle: Bundle, val contextHolder: ContextHolder): Request{
  internal  fun getAction():ActionDelegate?{
        val url=ParameterSupport.getUri(bundle)?:return null
        return ActionCenter.getAction(url)
    }
}

internal suspend fun NavigateParams.navigate(navigateInterceptor: NavigateInterceptor?=null):NavigateResult{
   return getAction()?.navigate(this,navigateInterceptor)?: NavigateResult.empty
}