package com.x930073498.component.annotations


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MethodBundleNameAnnotation(val name: String = "",val desc:String="") {
}