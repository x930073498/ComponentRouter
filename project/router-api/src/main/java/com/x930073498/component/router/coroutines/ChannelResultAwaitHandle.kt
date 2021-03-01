package com.x930073498.component.router.coroutines

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select


@Suppress("UNCHECKED_CAST")
open class ChannelResultAwaitHandle<T> constructor() : ResultAwaitHandle<T>, ResultSetterHandle<T> {
    private companion object DefaultResult


    private var _result: Any? = DefaultResult
    private val channel by lazy {
        Channel<T>(1)
    }

    constructor(result: T) : this() {
        _result = result
    }

    override suspend fun await(): T {

        return if (_result === DefaultResult) {
            select {
                channel.onReceive {
                    it
                }
            }
        } else {
            _result as T
        }
    }


    override fun setResult(result: T) {
        if (_result !== DefaultResult) return
        _result = result
        channel.offer(result)
    }

    override fun getOrNull(): T? {
        return if (_result === DefaultResult) {
            null
        } else {
            _result as T
        }
    }

    override fun getOrThrow(): T {
        return if (_result === DefaultResult) {
            throw RuntimeException("尚未执行setResult")
        } else {
            _result as T
        }
    }
}

