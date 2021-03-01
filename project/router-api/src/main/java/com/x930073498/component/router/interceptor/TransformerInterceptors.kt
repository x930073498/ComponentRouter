package com.x930073498.component.router.interceptor

@Suppress("UNCHECKED_CAST")
open class TransformerInterceptors<T, V>(
    private val requestToResponseTransformer: Transformer<T, V>,
    private val responseToRequestTransformer: Transformer<V, T>
) {
    private val interceptors = arrayListOf<Interceptor>()

    private fun size() = interceptors.size
    private var isStarted = false


    fun requestDirect(data: T): V {
        if (isStarted) throw InterceptorException("拦截器已经处于运行中")
        isStarted = true
        var request = data
        var response: V = requestToResponseTransformer.transform(request)
        var index = -1
        fun hasNext(): Boolean {
            index++
            return size() > index
        }
        runCatching {
            while (hasNext()) {
                response = interceptDirect(index, request)
                request = responseToRequestTransformer.transform(response)
            }
        }.onFailure {
            if (it !is InterceptException) throw it
        }
        return response
    }

    suspend fun requestCoroutine(data: T): V {
        if (isStarted) throw InterceptorException("拦截器已经处于运行中")
        isStarted = true
        var request = data
        var response: V = requestToResponseTransformer.transform(request)
        var index = -1
        fun hasNext(): Boolean {
            index++
            return size() > index
        }
        runCatching {
            while (hasNext()) {
                response = interceptCoroutine(index, request)
                request = responseToRequestTransformer.transform(response)
            }
        }.onFailure {
            if (it !is InterceptException) {
                throw  it
            }
        }
        return response
    }

    private fun interceptDirect(index: Int, data: T): V {
        val result = when (val interceptor = getInterceptor(index)) {
            is DirectInterceptor<*> -> {
                interceptor as DirectInterceptor<T>
                val chain = RealChain(data, index + 1, this)
                interceptor.intercept(chain).get()
            }
            else -> data
        }
        return requestToResponseTransformer.transform(result)
    }

    private suspend fun interceptCoroutine(index: Int, data: T): V {
        val chain = RealChain(data, index + 1, this)
        val result = when (val interceptor = getInterceptor(index)) {
            is CoroutineInterceptor<*> -> {
                interceptor as CoroutineInterceptor<T>
                interceptor.intercept(chain).get()
            }
            is DirectInterceptor<*> -> {
                interceptor as DirectInterceptor<T>
                interceptor.intercept(chain).get()
            }
            else -> {
                data
            }
        }
        return requestToResponseTransformer.transform(result)
    }

    internal fun getInterceptor(index: Int): Interceptor {
        return interceptors[index]
    }

    internal fun addInterceptor(index: Int, interceptor: Interceptor) {
        interceptors.add(index, interceptor)
    }


    fun addInterceptor(vararg interceptor: Interceptor) {
        interceptors.addAll(interceptor)
    }


}