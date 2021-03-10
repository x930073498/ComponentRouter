@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router.coroutines

import com.x930073498.component.auto.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference


fun <T> listenOf(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): ResultListenableFlow<T> {
    return ResultListenableImpl<Unit, T>(scope, coroutineContext)
}

fun <T> listenOf(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    data: T
): ResultListenable<T> {
    val result = ResultListenableImpl<Unit, T>(scope, coroutineContext)
    result.setResult(data)
    return result
}

fun <T> listenOf(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T
): ResultListenable<T> {
    return listenOf<Unit>(scope, coroutineContext, Unit).map {
        block()
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T, V> ResultListenable<T>.map(@BuilderInference transformer: Transformer<T, V>): ResultListenable<V> {
    return setter {
        setResult(transformer.transform(it))
    }
}

fun <T, V> ResultListenable<T>.cast(): ResultListenable<V> {
    return setter {
        setResult(it as V)
    }
}


fun <T, V> ResultListenable<T>.map(transform: suspend (T) -> V): ResultListenable<V> {
    return setter {
        setResult(transform(it))
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T, V> ResultListenable<T>.setter(@BuilderInference setterHandle: suspend ResultSetterHandle<V>. (T) -> Unit): ResultListenable<V> {
    val result = ResultListenableImpl<T, V>(this)
    result.sendAction {
        setterHandle(result, await())
    }
    return result
}

suspend fun <T> ResultListenable<T>.result(): T {
    val data = await()
    dispose()
    return data
}

fun <T> ResultListenable<T>.end(): DisposableHandle {
    disposeSafety()
    return start()
}
