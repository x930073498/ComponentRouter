package com.x930073498.component.router.coroutines

interface StarterHandle : DisposableHandle, Starter {
    override fun start(): StarterHandle
}
interface Starter{
    fun start(): Starter
    fun isStarted():Boolean
}