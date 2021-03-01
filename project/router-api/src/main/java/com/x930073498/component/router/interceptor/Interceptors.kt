package com.x930073498.component.router.interceptor

private class InstanceTransformer<V>: Transformer<V, V> {
    override fun transform(data: V): V {
        return data
    }

}

class Interceptors<V>:
    TransformerInterceptors<V, V>(InstanceTransformer<V>(), InstanceTransformer<V>())