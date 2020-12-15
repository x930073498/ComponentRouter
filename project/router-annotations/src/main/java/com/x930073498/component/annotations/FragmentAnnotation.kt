package com.x930073498.component.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target( AnnotationTarget.CLASS)
annotation class FragmentAnnotation(
    val group: String = "",
    val path: String,

    val interceptors: Array<String> = []
)