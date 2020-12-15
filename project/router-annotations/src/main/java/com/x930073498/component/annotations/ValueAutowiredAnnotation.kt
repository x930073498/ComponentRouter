package com.x930073498.component.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD,AnnotationTarget.PROPERTY_SETTER)
annotation class ValueAutowiredAnnotation(val name: String = "")