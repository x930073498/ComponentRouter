package com.x930073498.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class MethodAnnotation(
    val group: String = "",
    val path: String,
    val interceptors: Array<String> = [],
)