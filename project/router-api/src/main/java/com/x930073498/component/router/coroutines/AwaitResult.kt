package com.x930073498.component.router.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlin.coroutines.CoroutineContext

private sealed class AwaitResultAction<T> {
    class SetResult<T>(val result: T) : AwaitResultAction<T>()
    class Listen<T>(val async: Boolean = false, val listener: suspend (T) -> Unit) : AwaitResultAction<T>()
}

private val AwaitResultCoroutineScope  by lazy {
    CoroutineScope(Dispatchers.IO)
}

fun <T> createAwaitResult(scope: CoroutineScope? = null, coroutineContext: CoroutineContext? = null, result: T? = null): AwaitResult<T> {
    return AwaitResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext) {
        result
    }
}

fun <T> createAwaitResult(scope: CoroutineScope? = null, coroutineContext: CoroutineContext? = null, init: suspend () -> T): AwaitResult<T> {
    return AwaitResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext, null, init)
}


fun <T, R> AwaitResult<T>.map(transform: suspend (T) -> R): AwaitResult<R> {
    val result = createUpon<R>()
    listen(true) {
        result.setResult(transform(it))
    }
    return result
}


open class AwaitResult<T> protected constructor(defaultScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
                                                coroutineContext: CoroutineContext? = null,
                                                parentScope: CoroutineScope? = null,
                                                private val init: suspend () -> T? = { null }
) {
    companion object {
        internal fun <T> create(scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
                                coroutineContext: CoroutineContext? = null,
                                parent: CoroutineScope? = null,
                                init: suspend () -> T? = { null }): AwaitResult<T> {
            return AwaitResult(scope, coroutineContext, parent, init)
        }
    }

    private var result: T? = null
    private var hasResult = false
    private val channel = Channel<T>(1)
    private val actionChannel = Channel<AwaitResultAction<T>>(Channel.UNLIMITED)
    private val listeners = arrayListOf<AwaitResultAction.Listen<T>>()
    private val callback: suspend (T) -> Unit = { data ->
        coroutineScope {
            val list = ArrayList(listeners)
            val asyncList = list.filter {
                it.async
            }
            asyncList.forEach {
                async {
                    it.listener(data)
                }.start()
            }
            list.removeAll(asyncList)
            list.forEach {
                it.listener(data)
            }
        }

    }

    private var channelJob: Job
    private val parent: CoroutineScope = parentScope ?: defaultScope + SupervisorJob()
    private val currentCoroutineContext = with(coroutineContext) {
        if (this == null) parent.coroutineContext else parent.coroutineContext + this
    }

    init {
        channelJob = getListenJob(true)
    }

    fun <R> createUpon(): AwaitResult<R> {
        return create(parent, currentCoroutineContext, parent)
    }

    private fun getListenJob(isInit: Boolean): Job {
        return parent.launch(currentCoroutineContext) {
            if (isInit) {
                result = init()
                if (result != null) {
                    hasResult = true
                }
            }
            for (action in actionChannel) {
                runCatching {
                    when (action) {
                        is AwaitResultAction.Listen -> {
                            result?.let {
                                if (action.async) {
                                    async { action.listener(it) }.start()
                                } else {
                                    action.listener(it)
                                }
                            }
                            listeners.add(action)
                        }
                        is AwaitResultAction.SetResult -> {
                            val data = action.result
                            if (!hasResult) {
                                result = data
                                channel.send(data)
                                callback.invoke(data)
                            }
                            hasResult = true
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    fun setResult(result: T) {
        actionChannel.offer(AwaitResultAction.SetResult(result))
    }

    fun listen(async: Boolean = false, callback: suspend (T) -> Unit) {
        actionChannel.offer(AwaitResultAction.Listen(async, callback))
    }

    suspend fun await(): T {
        while (true) {
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

    fun destroy() {
        parent.cancel()
        channelJob.cancel()
        channel.cancel()
        actionChannel.cancel()
    }

    fun resetValue() {
        result = null
        hasResult = false
        channelJob.cancel()
        channelJob = getListenJob(false)
    }

}