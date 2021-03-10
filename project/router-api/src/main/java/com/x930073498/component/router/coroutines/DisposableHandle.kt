package com.x930073498.component.router.coroutines

interface DisposableHandle {
    fun dispose()
    fun disposeSafety()
    fun isDisposed():Boolean
}