package com.x930073498.component.auto.plugin.core

/**
 * @param classPath 不包含.class后缀
 */
data class ClassInfo(
    val key: String,
    val filePath: String,//文件路径
    val classPath: String,//class路径
    val name: String//class名
)