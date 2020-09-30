package com.x930073498.router.impl

import com.x930073498.router.interceptor.Chain
import com.x930073498.router.interceptor.Interceptor
import com.x930073498.router.request.RouterRequest
import com.x930073498.router.response.RouterResponse

interface RouterInterceptor : Interceptor<RouterRequest, RouterResponse, Chain<RouterRequest, RouterResponse>>