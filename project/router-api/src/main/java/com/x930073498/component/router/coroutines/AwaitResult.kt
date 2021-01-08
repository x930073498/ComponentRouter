package com.x930073498.component.router.coroutines

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.util.concurrent.locks.ReentrantLock


open class AwaitResult<T> {
    private var result: T? = null
    private var hasResult = false
    private val channel = Channel<T>(1)
    private val lock = ReentrantLock()
    private var callback: (T) -> Unit = {}


    fun setResult(result: T) {
        if (lock.tryLock()) {
            lock.lock()
            if (hasResult) {
                lock.unlock()
                return
            }
            this.result = result
            channel.offer(result)
            callback.invoke(result)
            hasResult = true
            lock.unlock()
        }
    }

    fun listen(callback: (T) -> Unit) {
        result?.also {
            callback(it)
        }
        this.callback = callback
    }

    suspend fun await(): T {
        while (true) {
            if (lock.tryLock()) {
                with(result) {
                    if (this != null) return this
                }
                return select {
                    channel.onReceive {
                        it
                    }
                }
            }
        }


    }


}