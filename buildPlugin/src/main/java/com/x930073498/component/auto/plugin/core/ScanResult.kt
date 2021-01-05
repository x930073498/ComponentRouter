package com.x930073498.component.auto.plugin.core

import java.lang.StringBuilder

class ScanResult internal constructor(
    val key: String,
    @Transient private val holder: InjectInfoHolder
) {
    internal val autoClasses = arrayListOf<ClassInfo>()
    internal var injectLocationMethod: MethodInfo? = null
    internal var classInjectorMethod: MethodInfo? = null


    fun toSimpleString(): String {
        val classString = autoClasses.fold(StringBuilder()) { builder, clazz ->
            builder.append("        ").append(clazz.toSimpleString()).append("\n")
        }
        return "{\nkey=$key,\ninjectLocationMethod=${injectLocationMethod?.toSimpleString()},\nclassInjectorMethod=${classInjectorMethod?.toSimpleString()},\nautoClasses=[\n${classString}]\n}\n"
    }

    fun addAutoClass(classInfo: ClassInfo) {
        if (autoClasses.contains(classInfo)) return
        autoClasses.add(classInfo)
    }


    fun setInjectLocationMethod(methodInfo: MethodInfo) {
        if (this.injectLocationMethod != null) {
            throw RuntimeException("同一key的代码注入位置必须唯一，${injectLocationMethod} 与  $methodInfo key 相同")
        }
        this.injectLocationMethod = methodInfo
        holder.putInjectLocation(methodInfo.filePath, this)
    }

    fun removeInjectLocationMethod() {
        this.injectLocationMethod = null
    }

    fun removeClassInjectorMethod() {
        this.classInjectorMethod = null
    }

    fun removeAutoClass(classInfo: ClassInfo) {
        autoClasses.remove(classInfo)
    }


    fun setClassInjectorMethod(methodInfo: MethodInfo) {
        if (this.classInjectorMethod != null) {
            throw RuntimeException("同一key的class 注入方法必须唯一，现${classInjectorMethod}  与 $methodInfo 冲突")
        }
        this.classInjectorMethod = methodInfo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanResult

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }


}