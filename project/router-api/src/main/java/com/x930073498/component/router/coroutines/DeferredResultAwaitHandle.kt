package com.x930073498.component.router.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.lang.RuntimeException


@Suppress("UNCHECKED_CAST")
open class DeferredResultAwaitHandle<T> :
    ResultAwaitHandle<T>, ResultSetter<T>,
    DisposableHandle {

    private var isDisposed = false

    private val deferred: CompletableDeferred<T> = CompletableDeferred()


    override suspend fun await(): T {
        return deferred.await()
    }


    override fun setResult(result: T) {
        runCatching {
            deferred.complete(result)
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun getOrNull(): T? {
        return runCatching {
            deferred.getCompleted()
        }.getOrNull()
    }

    override fun getOrThrow(): T {
        return deferred.getCompleted()
    }

    override fun dispose() {
        if (!deferred.isCompleted) {
            deferred.completeExceptionally(CancellationException("取消监听"))
        }
        isDisposed = true
    }

    override fun disposeSafety() {
        dispose()
    }

    override fun isDisposed(): Boolean {
        return isDisposed
    }
}

