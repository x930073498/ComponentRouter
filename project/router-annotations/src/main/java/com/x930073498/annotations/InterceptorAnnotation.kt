package com.x930073498.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class InterceptorAnnotation(
    val path: String,
    val group: String = ""
)