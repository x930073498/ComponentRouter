package com.x930073498.component.auto.plugin.core


data class ScanFileInfo(
    var filePath: String,
    var classInjectorMethods: List<MethodInfo> = arrayListOf(),
    var injectLocationMethods: List<MethodInfo> = arrayListOf(),
    var autoClasses: List<ClassInfo> = arrayListOf()

) {
    fun isEmpty(): Boolean =
        classInjectorMethods.isEmpty() && injectLocationMethods.isEmpty() && autoClasses.isEmpty()

    fun isNotEmpty() = !isEmpty()
    fun addClassInjector(methodInfo: MethodInfo) {
        if (classInjectorMethods.contains(methodInfo)) return
        classInjectorMethods += methodInfo
    }

    fun addInjectLocation(methodInfo: MethodInfo) {
        if (injectLocationMethods.contains(methodInfo)) return
        injectLocationMethods += methodInfo
    }

    fun addAutoClass(classInfo: ClassInfo) {
        if (autoClasses.contains(classInfo)) return
        autoClasses += classInfo
    }

    fun push(scanFileInfo: ScanFileInfo) {
        if (this.filePath != scanFileInfo.filePath) return
        scanFileInfo.classInjectorMethods.forEach {
            addClassInjector(it)
        }
        scanFileInfo.autoClasses.forEach {
            addAutoClass(it)
        }
        scanFileInfo.injectLocationMethods.forEach {
            addInjectLocation(it)
        }

    }


}

