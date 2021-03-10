package com.x930073498.component.router.coroutines


interface ResultListenableFlow<T>: ResultListenable<T>, ResultSetterHandle<T> {
    override fun listen(callback: suspend (T) -> Unit): ResultListenableFlow<T>

    override fun dispose()
    override fun start(): ResultListenableFlow<T>

}

