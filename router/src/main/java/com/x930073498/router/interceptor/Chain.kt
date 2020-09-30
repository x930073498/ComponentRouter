package com.x930073498.router.interceptor


interface Chain<T, V> : ChainSource<T> where T : Request, V : Response {


    suspend fun process(request: T): V

    fun addNext(interceptor: Interceptor<T, V, Chain<T, V>>)


}

interface ChainSource<T> where T : Request {
    suspend fun headerRequest(): T
    suspend fun request(): T
}


fun <T, V> Chain<T, V>.addNext(interceptor: suspend Chain<T, V>.() -> V) where T : Request, V : Response {
    addNext(object : Interceptor<T, V, Chain<T, V>> {
        override suspend fun intercept(chain: Chain<T, V>): V {
            return interceptor(chain)
        }
    })
}

