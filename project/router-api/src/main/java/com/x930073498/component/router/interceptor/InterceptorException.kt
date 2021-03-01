package com.x930073498.component.router.interceptor

internal open class InterceptorException(msg: String) : RuntimeException(msg)

internal class InterceptException : InterceptorException("Chain 中断")
internal class DisposeException : InterceptorException("Chain 取消")