package com.x930073498.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target( AnnotationTarget.CLASS)
annotation class InterceptorAnnotation(
    val authority: String = "",
    val path: String,
    val priority: Int = 0,
)