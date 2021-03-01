package com.x930073498.component.router.impl

import com.x930073498.component.router.interceptor.CoroutineInterceptor
import com.x930073498.component.router.request.RouterRequest

interface CoroutineRouterInterceptor : RouterInterceptor,
    CoroutineInterceptor<RouterRequest> {

}





