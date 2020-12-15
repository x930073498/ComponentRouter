package com.x930073498.component.router.action

import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.interceptor.Interceptor
import com.x930073498.component.router.interceptor.InternalInterceptors
import com.x930073498.component.router.interceptor.onInterceptors

typealias NavigateChain = Chain<NavigateParams, NavigateResult>

typealias NavigateInterceptor=Interceptor<NavigateParams,NavigateResult,NavigateChain>

internal fun toInterceptors(params: NavigateParams): InternalInterceptors<NavigateParams, NavigateResult> = params.onInterceptors()