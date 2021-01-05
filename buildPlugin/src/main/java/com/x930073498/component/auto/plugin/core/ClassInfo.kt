package com.x930073498.component.auto.plugin.core

import org.jetbrains.kotlin.konan.file.File

/**
 * @param classPath 不包含.class后缀
 */
data class ClassInfo(
    val key: String,
    val filePath: String,//文件路径
    val classPath: String,//class路径
    val name: String//class名
) {
    fun toSimpleString(): String {
        return classPath
    }

    fun getPackageName(): String {
        val index = classPath.lastIndexOf(File.separator)
        return if (index <= 0) ""
        else classPath.substring(0, index)
    }
}