package com.x930073498.router.interceptor

interface Interceptors< T,  V,  R> :
    ListHelper<T, V, R> where  T : Request, V : Response, R : Interceptors<T, V, R> {
    suspend fun start(): V
}

interface ListHelper< T, V, R> where  T : Request, V : Response, R : Interceptors<T, V, R> {
    fun add(vararg interceptor: Interceptor<T, V, Chain<T, V>>): R
    fun add(interceptor: suspend Chain<T, V>.() -> V): R
    fun add(index: Int, interceptor: suspend Chain<T, V>.() -> V): R
}

@Suppress("UNCHECKED_CAST")
abstract class AbsInterceptors<T, V, R> :
    Interceptors<T, V, R> where  T : Request, V : Response, R : Interceptors<T, V, R> {
    private val helper = InterceptorsHelper {
        this as R
    }

    override fun add(vararg interceptor: Interceptor<T, V, Chain<T, V>>): R {
        return helper.add(*interceptor)
    }

    override fun add(interceptor: suspend Chain<T, V>.() -> V): R {
        return helper.add(interceptor)
    }

    protected fun size(): Int {
        return helper.size()
    }

    protected fun get(index: Int): Interceptor<T, V, Chain<T, V>> {
        return helper.get(index)
    }

    override fun add(index: Int, interceptor: suspend Chain<T, V>.() -> V): R {
        return helper.add(index, interceptor)
    }
}

@Suppress("UNCHECKED_CAST")
class InterceptorsHelper<T, V, R> internal constructor(private val target: () -> R) :
    ListHelper<T, V, R> where  T : Request, V : Response, R : Interceptors<T, V, R> {
    private val interceptors = arrayListOf<Interceptor<T, V, Chain<T, V>>>()
    override fun add(vararg interceptor: Interceptor<T, V, Chain<T, V>>): R {
        interceptors += interceptor
        return target()
    }

    override fun add(interceptor: suspend Chain<T, V>.() -> V): R {
        interceptors += interceptor.toInterceptor()
        return target()
    }

    fun get(index: Int): Interceptor<T, V, Chain<T, V>> {
        return interceptors[index]
    }

    fun size(): Int {
        return interceptors.size
    }

    override fun add(index: Int, interceptor: suspend Chain<T, V>.() -> V): R {
        interceptors.add(index, interceptor.toInterceptor())
        return target()
    }

}


fun <T, V> T.onInterceptors(processBlock: suspend ChainSource<T>.() -> V): InternalInterceptors<T, V> where T : Request, V : Response {
    return InternalInterceptors(this, processBlock)
}



class InternalInterceptors<T, V> internal constructor(
    private val request: T, private val processBlock: suspend ChainSource<T>.() -> V,
) : AbsInterceptors<T, V, InternalInterceptors<T, V>>() where T : Request, V : Response {
    private var beforeInterceptBlock: suspend Chain<T, V>.() -> Unit = {}

    private var hasStart = false
    fun beforeIntercept(block: suspend Chain<T, V>.() -> Unit): InternalInterceptors<T, V> {
        beforeInterceptBlock = block
        return this
    }


    override suspend fun start(): V {
        if (hasStart) throw RuntimeException("所有拦截器已经执行过")
        hasStart = true
        add {
            val header = headerRequest()
            val request = request()
            val chainSource = object : ChainSource<T> {
                override suspend fun headerRequest(): T {
                    return header
                }

                override suspend fun request(): T {
                    return request
                }
            }
            processBlock(chainSource)
        }
        val chain = RealChain(0, this, request, this.request, beforeInterceptBlock)
        return chain.process(request)
    }

    private suspend fun intercept(index: Int, request: T): V {
        val next = RealChain(index + 1, this, request, this.request, beforeInterceptBlock)
        val interceptor = get(index)
        return interceptor.intercept(next)
    }


    internal class RealChain<I, T, V> internal constructor(
        private val index: Int = 0,
        private val interceptors: InternalInterceptors<T, V>,
        private val request: T,
        private val originalRequest: T,
        private val beforeBlock: suspend Chain<T, V>.() -> Unit,
    ) : Chain<T, V> where I : Interceptor<T, V, Chain<T, V>>, T : Request, V : Response {
        private var calls = 0
        override suspend fun request(): T {
            return request
        }

        override suspend fun process(request: T): V {
            calls++
            check(calls <= 1) { "interceptor ${interceptors.get(index - 1)} must call proceed() exactly once" }
            beforeBlock()
            return interceptors.intercept(index, request)
        }

        override fun addNext(interceptor: Interceptor<T, V, Chain<T, V>>) {
            interceptors.add(index) {
                interceptor.intercept(this)
            }
        }

        override suspend fun headerRequest(): T {
            return originalRequest
        }


    }


}


fun <T, V, R> Interceptors<T, V, R>.add(interceptors: List<Interceptor<T, V, Chain<T, V>>>): R where  T : Request, V : Response, R : Interceptors<T, V, R> {
    return add(*interceptors.toTypedArray())
}