package com.x930073498.component.router.interceptor

internal open class InterceptorException(msg: String) : RuntimeException(msg)

internal class InterceptException : InterceptorException("Chain 中断")
internal class DisposeException : InterceptorException("Chain 取消")
internal class DataStateChangeException(private val data: Any?) : InterceptorException("Data 状态已经发生改变") {
    internal fun <T> getData(): T {
        return data as T
    }
}