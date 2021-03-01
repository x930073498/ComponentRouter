package com.x930073498.component.router.interceptor

interface Interceptor

 interface CoroutineInterceptor<T> : Interceptor {
    suspend fun intercept(chain: Chain<T>):Chain.ChainResult<T>
}

interface DirectInterceptor<T>: Interceptor {

    fun intercept(chain: Chain<T>):Chain.ChainResult<T>


}
