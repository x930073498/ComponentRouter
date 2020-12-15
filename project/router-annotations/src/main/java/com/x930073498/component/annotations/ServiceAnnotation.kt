package com.x930073498.component.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ServiceAnnotation(
    val group: String = "",
    val path: String,
    val singleton: Boolean = true,
    val autoInvoke: Boolean = true,
    val interceptors: Array<String> = [],
)