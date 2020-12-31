package com.x930073498.component.auto.plugin.inject

import com.google.gson.Gson
import org.gradle.api.Project
import org.objectweb.asm.*
import java.io.File
import java.io.InputStream


const val ANNOTATION_CLASS_SIGNATURE = "Lcom/x930073498/component/auto/AutoClass;"
const val ANNOTATION_METHOD_CLASS_INJECTOR_SIGNATURE =
    "Lcom/x930073498/component/auto/ClassInjector;"
const val ANNOTATION_METHOD_INJECT_TO_SIGNATURE = "Lcom/x930073498/component/auto/InjectLocation;"


object Injector {
    private val map: MutableMap<String, InjectHolder> = mutableMapOf()


    fun loadCache(project: Project) {
        InjectHolder.loadCache(project)
        map.clear()
        val temp = read<LinkedHashMap<String, InjectHolder>>(
            Gson(),
            getCacheFile(project, "injectorMap.json")
        ) ?: return
        map.putAll(temp)
    }

    fun saveCache(project: Project) {
        InjectHolder.saveCache(project)
        val temp = LinkedHashMap(map)
        write(Gson(), getCacheFile(project, "injectorMap.json"), temp)
    }

    private fun getInjectHolderByInjectTargetMethod(
        classPath: String,
        name: String,
        descriptor: String
    ): InjectHolder? {
        return InjectHolder.getByTargetMethod(MethodInfo(classPath, name, descriptor))
    }

    fun setRegisterMethod(key: String, methodInjectInfo: MethodInfo) {
        var info = map[key]
        if (info == null) {
            info = InjectHolder(key)
            map[key] = info
        }
        info.setRegisterMethod(methodInjectInfo)
    }

    fun setInjectTargetMethod(key: String, methodInjectInfo: MethodInfo) {
        var info = map[key]
        if (info == null) {
            info = InjectHolder(key)
            map[key] = info
        }
        info.setInjectTargetMethod(methodInjectInfo)
    }

    fun addRegisterClass(key: String, classInjectInfo: ClassInfo) {
        var info = map[key]
        if (info == null) {
            info = InjectHolder(key)
            map[key] = info
        }
        info.addRegisterClass(classInjectInfo)
    }

    internal fun inject(
        mv: MethodVisitor,
        classPath: String,
        name: String,
        descriptor: String
    ): MethodVisitor {
        val holder = getInjectHolderByInjectTargetMethod(classPath, name, descriptor)
        if (holder != null) {
            return InjectMethodVisitor(Opcodes.ASM6, mv, holder)
        }
        return mv
    }


    fun inject(classPath: String, inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = InjectClassVisitor(Opcodes.ASM6, cw, classPath)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    fun getScanClassVisitor(api: Int, cv: ClassVisitor, classPath: String): ClassVisitor {
        return ScanClassVisitor(api, cv, classPath)
    }

    fun getInjectClassVisitor(api: Int, cv: ClassVisitor, classPath: String): ClassVisitor {
        return InjectClassVisitor(api, cv, classPath)
    }

    fun reset(classPath: String) {
        val removed = InjectHolder.clear(classPath) ?: return
        map.remove(removed.key)
    }

    fun shouldGenerate(classPath: String): Boolean {
        return InjectHolder.shouldGenerateCodeInClassPath(classPath)
    }

    fun scan(classPath: String, inputStream: InputStream) {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = ScanClassVisitor(Opcodes.ASM6, cw, classPath)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
    }


}









