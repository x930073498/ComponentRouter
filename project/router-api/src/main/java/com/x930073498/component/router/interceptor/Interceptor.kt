package com.x930073498.component.router.interceptor


interface Interceptor< T, V, S> where T : Request, V : Response, S : Chain<T, V> {
    suspend fun intercept(chain: S): V
}

internal fun <T, V, S> (suspend S.() -> V).toInterceptor() where T : Request, V : Response, S : Chain<T, V> =
    object : Interceptor<T, V, S> {
        override suspend fun intercept(chain: S): V {
            return invoke(chain)
        }

    }


