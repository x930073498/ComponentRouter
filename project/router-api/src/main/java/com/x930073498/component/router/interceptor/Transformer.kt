package com.x930073498.component.router.interceptor
fun interface Transformer<T, V> {
     fun transform(data: T): V
}
