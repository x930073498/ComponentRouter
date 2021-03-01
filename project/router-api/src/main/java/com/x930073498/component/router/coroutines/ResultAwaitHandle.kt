package com.x930073498.component.router.coroutines

interface ResultAwaitHandle<T> {
    suspend fun await(): T
    fun getOrNull():T?
    fun getOrThrow():T

}