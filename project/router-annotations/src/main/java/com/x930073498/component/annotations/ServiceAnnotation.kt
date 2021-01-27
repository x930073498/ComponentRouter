package com.x930073498.component.annotations


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ServiceAnnotation(
    val group: String = "",
    val path: String,
    val desc: String = "",
    val autoRegister:Boolean=true,
    val singleton: Boolean = true,
    val autoInvoke: Boolean = true,
    val interceptors: Array<String> = [],
)