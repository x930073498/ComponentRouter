package com.x930073498.component.router.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext


interface ResultListenable<T> : DisposableHandle,
    StarterHandle,
    ActionHandle,
    ResultHandle<T> {

    override fun listen(callback: suspend (T) -> Unit): ResultListenable<T>
    override fun start(): ResultListenable<T>
    val coroutineScope: CoroutineScope
    val coroutineContext:CoroutineContext

}

