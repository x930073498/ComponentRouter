package com.x930073498.component.auto.plugin.core

/**
 * @param classPath 不包含.class后缀
 */
data class MethodInfo(
    val key: String,
    val filePath: String,//文件路径
    val classPath: String,//class 路径
    val name: String,//方法名
    val descriptor: String,//方法描述
    val isStatic: Boolean = false//是否静态方法
)
