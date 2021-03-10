package com.x930073498.component.router.coroutines
interface Transformer<T, V> {
    suspend fun transform(data: T): V
}
