package com.x930073498.component.router.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * T parent
 * V this
 */
internal class ResultListenableImpl<T, V> internal constructor(
    override val coroutineScope: CoroutineScope,
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
) :
    ResultListenableFlow<V> {
    private var _parent: ResultListenable<T>? = null
    private val queue = CoroutineActionQueue(coroutineScope, coroutineContext)
    private val awaitHandle = DeferredResultAwaitHandle<V>()
    private val tree: HandleTree by lazy {
        val parent = _parent
        if (parent is ResultListenableImpl<*, *>) {
            HandleTree(queue, parent.tree, awaitHandle)
        } else {
            HandleTree(queue, null, awaitHandle)
        }
    }


    private fun createTree() = tree

    constructor (parent: ResultListenable<T>) : this(
        parent.coroutineScope,
        parent.coroutineContext
    ) {
        _parent = parent
        createTree()
    }


    override fun disposeSafety() {
        tree.disposeSafety()
    }

    override fun isDisposed(): Boolean {
        return tree.isDisposed()
    }

    override fun isStarted(): Boolean {
        return tree.isStarted()
    }

    override fun sendAction(action: ActionHandle.Action) {
        tree.sendAction(action)
    }

    override suspend fun await(): V {
        return awaitHandle.await()
    }

    override fun listen(callback: suspend (V) -> Unit): ResultListenableFlow<V> {
        val result = ResultListenableImpl<V, V>(this)
        result.sendAction {
            val data = await()
            callback(data)
            result.setResult(data)
        }
        return result
    }

    override fun dispose() {
        tree.dispose()
    }

    override fun start(): ResultListenableFlow<V> {
        tree.start()
        return this
    }

    override fun setResult(result: V) {
        awaitHandle.setResult(result)
    }

    override fun getOrNull(): V? {
        return awaitHandle.getOrNull()
    }

    override fun getOrThrow(): V {
        return awaitHandle.getOrThrow()
    }


}