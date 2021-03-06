package com.x930073498.component.auto.annotations

/**
 * 标记注册方法
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class InjectLocation(val key:String)
