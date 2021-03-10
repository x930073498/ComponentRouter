package com.x930073498.component.router.interceptor

interface Chain<T> {

    fun process(data: T): ChainResult<T>

    fun intercept(): ChainResult<T>

    fun dispose(): ChainResult<T>

    fun request(): T


    fun addNext(interceptor: Interceptor)

    class ChainResult<T> internal constructor(private val result: T){
        fun get():T{
            return result
        }
    }

}

fun <T> Chain<T>.addDirect(block: (Chain<T>) -> Chain.ChainResult<T>) {
    val interceptor = object : DirectInterceptor<T> {
        override fun intercept(chain: Chain<T>): Chain.ChainResult<T> {
           return block(chain)
        }
    }
    addNext(interceptor)
}

fun <T> Chain<T>.addCoroutine(block: suspend (Chain<T>) -> Chain.ChainResult<T>) {
    val interceptor = object : CoroutineInterceptor<T> {
        override suspend fun intercept(chain: Chain<T>): Chain.ChainResult<T> {
            return block(chain)
        }
    }
    addNext(interceptor)
}

