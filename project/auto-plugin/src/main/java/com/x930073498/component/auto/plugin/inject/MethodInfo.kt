package com.x930073498.component.auto.plugin.inject

/**
 * @param classPath 不包含.class后缀
 */
data class MethodInfo(
    val classPath: String,
    val name: String,
    val descriptor: String,
    val isStatic: Boolean = false
)
