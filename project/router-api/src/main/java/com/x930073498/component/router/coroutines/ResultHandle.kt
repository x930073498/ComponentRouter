package com.x930073498.component.router.coroutines

interface ResultHandle<T>: ResultAwaitHandle<T>, ResultCallback<T> {
    override fun listen(callback: suspend (T) -> Unit): ResultHandle<T>
}