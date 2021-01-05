package com.x930073498.component.auto.annotations

/**
 * 注册类的方法
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ClassInjector(val key: String)
