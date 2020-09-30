package com.x930073498.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD,AnnotationTarget.PROPERTY_SETTER)
annotation class ValueAutowiredAnnotation(val name: String = "")