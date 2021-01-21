package com.x930073498.component.router.coroutines

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.selects.select
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

sealed class AwaitAction<T> {
    /**
     * 设置result
     */
    internal class SetResult<T>(val result: T) : AwaitAction<T>()

    /**
     * 设置监听
     */
    internal class Listen<T>(val listener: suspend (T) -> Unit) :
        AwaitAction<T>()

    /**
     * 强制结束
     */
    internal class EndForce<T> : AwaitAction<T>()

    /**
     * 等待所有任务完成后结束
     */
    internal class EndUntilAllTaskCompleted<T> : AwaitAction<T>()

}

val AwaitResultCoroutineScope: CoroutineScope
    get() {
        return CoroutineScope(Dispatchers.IO)
    }

fun <T> resultOf(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null,
    result: T
): ResultListenable<T> {
    return CoroutineResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext) {
        result
    }
}

fun <T> resultOf(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null
): CoroutineResult<T> {
    return CoroutineResult.create(scope ?: AwaitResultCoroutineScope, coroutineContext)
}

fun <T> resultOf(
    scope: CoroutineScope? = null,
    coroutineContext: CoroutineContext? = null,
    init: suspend () -> T
): ResultListenable<T> {
    return CoroutineResult.create(
        scope ?: AwaitResultCoroutineScope,
        coroutineContext,
        null,
        null,
        init
    )
}

fun <T> T.bindLifecycle(lifecycle: Lifecycle): T where T : DisposableHandle {
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
        dispose()
    } else
        Dispatchers.Main.immediate.asExecutor().execute {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        dispose()
                    }
                }

            })
        }
    return this
}

fun <T> T.bindLifecycle(lifecycleOwner: LifecycleOwner): T where T : DisposableHandle {
    return bindLifecycle(lifecycleOwner.lifecycle)
}

fun <T, R> ResultListenable<T>.map(
    transform: suspend (T) -> R
): ResultListenable<R> {
    return createUpon {
        setResult(transform(it))
    }
}

fun <T> ResultListenable<T>.forceEnd(action: suspend (T) -> Unit = {}): DisposableHandle {
    return createUpon<Unit> {
        setResult(action(it))
    }.listen {
        sendAction(AwaitAction.EndForce())
    }
}

fun <T> ResultListenable<T>.end(action: suspend (T) -> Unit = {}): DisposableHandle {
    return createUpon<Unit> {
        setResult(action(it))
    }.listen {
        sendAction(AwaitAction.EndUntilAllTaskCompleted())
    }
}


fun <T, R> ResultListenable<T>.flatMap(

    transform: suspend (T) -> ResultListenable<R>
): ResultListenable<R> {
    return createUpon {
        setResult(transform(it).await())
    }
}


fun <T, R> ResultListenable<T>.cast(): ResultListenable<R> {
    return map {
        @Suppress("UNCHECKED_CAST")
        it as R
    }
}

interface ResultListenable<T> : DisposableHandle, ActionHandle<T> {
    suspend fun await(): T
    fun <R> createUpon(
        setter: suspend ResultSetter<R>.(T) -> Unit
    ): ResultListenable<R>

    override fun sendAction(action: AwaitAction<T>): ResultListenable<T>
    override fun invokeOnCancel(action: (Throwable?) -> Unit): ResultListenable<T>
    fun listen(callback: suspend (T) -> Unit): ResultListenable<T>

}

interface ResultSetter<T> : DisposableHandle, ActionHandle<T> {
    fun setResult(result: T): ResultListenable<T>
    override fun sendAction(action: AwaitAction<T>): ResultSetter<T>
    override fun invokeOnCancel(action: (Throwable?) -> Unit): ResultSetter<T>
}


interface DisposableHandle {
    fun dispose()
    fun hasResult(): Boolean
    fun invokeOnCancel(action: (Throwable?) -> Unit): DisposableHandle
}

interface ActionHandle<T> {
    fun sendAction(action: AwaitAction<T>): ActionHandle<T>
    fun dispose()
}


@Suppress("UNCHECKED_CAST")
open class CoroutineResult<T : Any?> protected constructor(
    defaultScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext? = null,
    parentScope: CoroutineScope? = null,
    parentHandle: DisposableHandle? = null,
    private val init: suspend () -> T? = {
        throw Exception("nothing")
    }
) : ResultListenable<T>, ResultSetter<T>,
    DisposableHandle {
    companion object {
        internal fun <T> create(
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
            coroutineContext: CoroutineContext? = null,
            parent: CoroutineScope? = null,
            parentHandle: DisposableHandle? = null,
            init: suspend () -> T? = { throw Exception("nothing") }
        ): CoroutineResult<T> {
            return CoroutineResult(scope, coroutineContext, parent, parentHandle, init)
        }
    }

    private val parentHandleRef = WeakReference(parentHandle)
    private var result: T? = null
    private var hasResult = false
    private var isCanceled = false
    private val channel = Channel<T>(1)
    private val actionChannel = Channel<AwaitAction<T>>(Channel.BUFFERED)
    private val listeners = arrayListOf<AwaitAction.Listen<T>>()

    private val channelJob: Job
    private val parent: CoroutineScope = parentScope ?: defaultScope + Job()
    private val currentCoroutineContext = with(coroutineContext) {
        if (this == null) parent.coroutineContext else parent.coroutineContext + this
    }
    private val listenJobs = CopyOnWriteArrayList(arrayListOf<Job>())

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
                        is AwaitAction.Listen -> {
                            if (hasResult) {
                                val job = async { action.listener(result as T) }
                                listenJobs.add(job)
                                job.invokeOnCompletion {
                                    listenJobs.remove(job)
                                }
                            } else listeners.add(action)
                        }
                        is AwaitAction.SetResult -> {
                            if (!hasResult) {
                                val data = action.result
                                hasResult = true
                                result = data
                                channel.send(data)
                                listeners.forEach {
                                    val job = async {
                                        it.listener(data)
                                    }
                                    listenJobs.add(job)
                                    job.invokeOnCompletion {
                                        listenJobs.remove(job)
                                    }

                                }
                                listeners.clear()
                            }
                            hasResult = true
                        }
                        is AwaitAction.EndForce -> {
                            cancelInternal()
                        }
                        is AwaitAction.EndUntilAllTaskCompleted -> {
                            listenJobs.forEach {
                                it.join()
                            }
                            cancelInternal()
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    override fun setResult(result: T): CoroutineResult<T> {
        return sendAction(AwaitAction.SetResult(result))
    }


    override fun listen(
        callback: suspend (T) -> Unit
    ): ResultListenable<T> {
        return sendAction(AwaitAction.Listen(callback))
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

    override fun dispose() {
        cancelInternal()
    }

    private fun cancelInternal() {
        isCanceled = true
        parentHandleRef.get()?.dispose()
        channelJob.cancel()
        channel.close()
        actionChannel.close()
        listeners.clear()
        listenJobs.clear()
    }

    override fun hasResult(): Boolean {
        return hasResult
    }

    override fun invokeOnCancel(action: (Throwable?) -> Unit): CoroutineResult<T> {
        channelJob.invokeOnCompletion(action)
        return this
    }


    override fun sendAction(action: AwaitAction<T>): CoroutineResult<T> {
        if (isCanceled) return this
        actionChannel.offer(action)
        return this
    }

    override fun <R> createUpon(
        setter: suspend ResultSetter<R>.(T) -> Unit
    ): ResultListenable<R> {
        val result =
            create<R>(parent, currentCoroutineContext, parentHandle = this, parent = parent)
        listen {
            setter(result, it)
        }
        return result
    }


}