package com.x930073498.component.router.coroutines

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

private sealed class AwaitResultAction<T> {
    class SetResult<T>(val result: T) : AwaitResultAction<T>()
    class Listen<T>(val async: Boolean = false, val listener: suspend (T) -> Unit) :
        AwaitResultAction<T>()
}

val AwaitResultCoroutineScope: CoroutineScope
    get() {
        return CoroutineScope(Dispatchers.IO)
    }

fun <T> createAwaitResult(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null,
    result: T
): ResultListenable<T> {
    return AwaitResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext) {
        result
    }
}

fun <T> createAwaitResult(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null
): AwaitResult<T> {
    return AwaitResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext)
}

fun <T> createAwaitResult(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null,
    init: suspend () -> T
): ResultListenable<T> {
    return AwaitResult.create(
        scope ?: AwaitResultCoroutineScope,
        coroutineContext,
        null,
        null,
        init
    )
}
//fun <T> createAwaitResult(
//    scope: CoroutineScope? = null,
//    coroutineContext: CoroutineContext? = null,
//    init: suspend ResultSetter<T>.() -> Unit
//): ResultListenable<T> {
//    return AwaitResult.create(
//        scope ?: AwaitResultCoroutineScope,
//        coroutineContext,
//        null,
//        null,
//        init
//    )
//}

fun <T> T.bindLifecycle(lifecycle: Lifecycle): T where T : Cancelable {
    Dispatchers.Main.immediate.asExecutor().execute {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            cancel()
        } else
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        cancel()
                    }
                }

            })
    }
    return this
}

fun <T> T.bindLifecycle(lifecycleOwner: LifecycleOwner): T where T : Cancelable {
    return bindLifecycle(lifecycleOwner.lifecycle)
}

fun <T, R> ResultListenable<T>.map(
    async: Boolean = false,
    transform: suspend (T) -> R
): ResultListenable<R> {
    return createUpon(async) {
        setResult(transform(it))
    }
}

fun <T> ResultListenable<T>.end(async: Boolean = false, action: suspend (T) -> Unit): Cancelable {
    return createUpon<Unit>(async) {
        setResult(action(it))
    }
}

fun <T, R> ResultListenable<T>.flatMap(
    async: Boolean = false,
    transform: suspend (T) -> ResultListenable<R>
): ResultListenable<R> {
    return createUpon(async) {
        setResult(transform(it).await())
    }
}

fun <T, R> ResultListenable<T>.cast(): ResultListenable<R> {
    return map {
        @Suppress("UNCHECKED_CAST")
        it as R
    }
}

interface ResultListenable<T> : Cancelable {
    suspend fun await(): T
    fun <R> createUpon(
        async: Boolean = false,
        setter: suspend ResultSetter<R>.(T) -> Unit
    ): ResultListenable<R>

    fun listen(async: Boolean = false, callback: suspend (T) -> Unit): ResultListenable<T>

}

interface ResultSetter<T> : Cancelable {
    fun setResult(result: T): ResultListenable<T>
}


interface Cancelable {
    fun cancel()
    fun hasResult(): Boolean
}

@Suppress("UNCHECKED_CAST")
open class AwaitResult<T : Any?> protected constructor(
    defaultScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext? = null,
    parentScope: CoroutineScope? = null,
    parentHandle: Cancelable? = null,
    private val init: suspend () -> T? = {
        throw Exception("nothing")
    }
) : ResultListenable<T>, ResultSetter<T>, Cancelable {
    companion object {
        internal fun <T> create(
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
            coroutineContext: CoroutineContext? = null,
            parent: CoroutineScope? = null,
            parentHandle: Cancelable? = null,
            init: suspend () -> T? = { throw Exception("nothing") }
        ): AwaitResult<T> {
            return AwaitResult(scope, coroutineContext, parent, parentHandle, init)
        }
    }

    private val parentHandleRef = WeakReference(parentHandle)
    private var result: T? = null
    private var hasResult = false
    private var isCanceled = false
    private val channel = Channel<T>(1)
    private val actionChannel = Channel<AwaitResultAction<T>>(Channel.BUFFERED)
    private val listeners = CopyOnWriteArrayList(arrayListOf<AwaitResultAction.Listen<T>>())
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
        listeners.clear()

    }

    private var channelJob: Job
    private val parent: CoroutineScope = parentScope ?: defaultScope + Job()
    private val currentCoroutineContext = with(coroutineContext) {
        if (this == null) parent.coroutineContext else parent.coroutineContext + this
    }

    init {
        channelJob = getListenJob()
    }


    private fun getListenJob(): Job {
        return parent.launch(currentCoroutineContext) {
            runCatching {
                result = init()
            }.onSuccess {
                hasResult = true
            }.onFailure {
                if (it.message != "nothing") {
                    hasResult = true
                }
            }
            for (action in actionChannel) {
                runCatching {
                    when (action) {
                        is AwaitResultAction.Listen -> {
                            if (hasResult) {
                                if (action.async) {
                                    async { action.listener(result as T) }.start()
                                } else {
                                    action.listener(result as T)
                                }
                            } else listeners.add(action)
                        }
                        is AwaitResultAction.SetResult -> {
                            if (!hasResult) {
                                val data = action.result
                                hasResult = true
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

    override fun setResult(result: T): ResultListenable<T> {
        if (isCanceled) return this
        actionChannel.offer(AwaitResultAction.SetResult(result))
        return this
    }


    override fun listen(
        async: Boolean,
        callback: suspend (T) -> Unit
    ): ResultListenable<T> {
        if (!isCanceled)
            actionChannel.offer(AwaitResultAction.Listen(async, callback))
        return this
    }

    override suspend fun await(): T {
        while (true) {
            if (hasResult) return result as T
            return select {
                channel.onReceive {
                    it
                }
            }
        }
    }

    override fun cancel() {
        isCanceled = true
        parentHandleRef.get()?.cancel()
        channelJob.cancel()
        channel.close()
        actionChannel.close()
        listeners.clear()
    }

    override fun hasResult(): Boolean {
        return hasResult
    }


    override fun <R> createUpon(
        async: Boolean,
        setter: suspend ResultSetter<R>.(T) -> Unit
    ): ResultListenable<R> {
        val result =
            create<R>(parent, currentCoroutineContext, parentHandle = this, parent = parent)
        listen(async) {
            setter(result, it)
        }
        return result
    }

}