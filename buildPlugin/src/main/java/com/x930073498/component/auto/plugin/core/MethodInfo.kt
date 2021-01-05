package com.x930073498.component.auto.plugin.core

import com.x930073498.component.auto.plugin.asIs
import org.jetbrains.kotlin.konan.file.File
import org.objectweb.asm.Opcodes

/**
 * @param classPath 不包含.class后缀
 */
data class MethodInfo(
    val key: String,
    val filePath: String,//文件路径
    val classPath: String,//class 路径
    val name: String,//方法名
    val descriptor: String,//方法描述
    val access: Int //标识
) {

    fun toSimpleString(): String {
        return "$classPath.$name$descriptor"
    }

    val isStatic = asIs(access, Opcodes.ACC_STATIC)
    val isPublic = asIs(access, Opcodes.ACC_PUBLIC)
    val isPrivate = asIs(access, Opcodes.ACC_PRIVATE)
    val isProtected = asIs(access, Opcodes.ACC_PROTECTED)
    fun getPackageName(): String {
        val index = classPath.lastIndexOf(File.separator)
        return if (index <= 0) ""
        else classPath.substring(0, index)
    }
}
