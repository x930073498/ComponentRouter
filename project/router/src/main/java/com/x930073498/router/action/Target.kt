package com.x930073498.router.action

import com.x930073498.router.impl.MethodInvoker

@Suppress("UNCHECKED_CAST")
abstract class Target<T>(
    val targetClazz: Class<T>,
) {
    companion object {
        private val providerMap = mutableMapOf<Class<*>, Any?>()
        private val methodMap = mutableMapOf<Class<*>, Any?>()
        internal fun putProviderSingleton(clazz: Class<*>, any: Any?) {
            providerMap[clazz] = any
        }

        internal fun <T> getProviderSingleton(clazz: Class<T>): T? {
            return providerMap[clazz] as? T
        }

        internal fun putMethod(clazz: Class<*>, any: Any?) {
            methodMap[clazz] = any

        }

        internal fun <T> getMethod(clazz: Class<T>): T? where T : MethodInvoker<*> {
            return methodMap[clazz] as? T
        }


    }
}

class ServiceTarget<T>(targetClazz: Class<T>, val isSingleTon: Boolean) :
    Target<T>( targetClazz)

class MethodTarget<T, R>(targetClazz: Class<T>, val methodInvokerType: Class<R>) :
    Target<T>(targetClazz) where R : MethodInvoker<T>


class ActivityTarget<T>(targetClazz: Class<T>) : Target<T>(targetClazz)

class FragmentTarget<T>(targetClazz: Class<T>) : Target<T>( targetClazz) {

}



