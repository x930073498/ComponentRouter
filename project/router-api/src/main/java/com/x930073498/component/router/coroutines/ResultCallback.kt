package com.x930073498.component.router.coroutines

interface ResultCallback<T> {
    fun listen(callback: suspend (T) -> Unit): ResultCallback<T>
}