package com.x930073498.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ServiceAnnotation(
    val group: String = "",
    val path: String,
    val singleton: Boolean = true,
    val autoInvoke: Boolean = true,
    val interceptors: Array<KClass<*>> = [],
    val interceptorsPath: Array<String> = [],
)