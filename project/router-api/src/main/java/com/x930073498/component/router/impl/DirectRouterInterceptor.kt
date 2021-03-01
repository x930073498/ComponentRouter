package com.x930073498.component.router.impl

import com.x930073498.component.router.interceptor.DirectInterceptor
import com.x930073498.component.router.request.RouterRequest

interface DirectRouterInterceptor : RouterInterceptor,
    DirectInterceptor<RouterRequest> {

}





