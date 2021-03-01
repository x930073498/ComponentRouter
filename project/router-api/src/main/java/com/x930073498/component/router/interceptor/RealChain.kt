package com.x930073498.component.router.interceptor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select


@Suppress("UNCHECKED_CAST")
internal class RealChain<T, V>(
    private val lastData: T,
    private val index: Int,
    private val interceptors: TransformerInterceptors<T, V>
) :
    Chain<T> {
    private companion object DefaultData


    private var calls = 0

    override fun process(data: T): Chain.ChainResult<T> {
        calls++
        check(calls <= 1) { "interceptor ${interceptors.getInterceptor(index - 1)} must call proceed() exactly once" }
        return Chain.ChainResult(data)
    }

    override fun request(): T {
        return lastData
    }


    override fun addNext(interceptor: Interceptor) {
        interceptors.addInterceptor(index, interceptor)
    }



    override fun intercept(): Chain.ChainResult<T> {
        throw InterceptException()
    }

    override fun dispose(): Chain.ChainResult<T> {
        throw DisposeException()
    }


}