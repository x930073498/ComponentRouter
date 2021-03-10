package com.x930073498.component.router.interceptor

fun interface DataStateChange<T> {
    fun isStateChanged(oldData: T, newData: T): Boolean
}