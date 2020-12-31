package com.x930073498.component.auto.plugin.inject

import com.google.gson.Gson
import org.gradle.api.Project
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InjectHolder(
    val key: String,
    var injectTargetMethodInfo: MethodInfo? = null,
    var registerMethodInfo: MethodInfo? = null,
    val classes: MutableList<ClassInfo> = arrayListOf<ClassInfo>()
) {


    companion object {

        private val map = mutableMapOf<MethodInfo, InjectHolder>()
        private val classes = mutableMapOf<String, InjectHolder>()

        internal fun loadCache(project: Project) {
            map.clear()
            classes.clear()
            val gson = Gson()
            val tempMap = read<LinkedHashMap<MethodInfo, InjectHolder>>(
                gson,
                getCacheFile(project, "injectorHolderMap.json")
            )
            if (tempMap != null) {
                map.putAll(tempMap)
            }
            val tempClasses = read<LinkedHashMap<String, InjectHolder>>(
                gson,
                getCacheFile(project, "injectorHolderClasses.json")
            )
            if (tempClasses != null) {
                classes.putAll(tempClasses)
            }

        }

        internal fun saveCache(project: Project) {
            val gson = Gson()
            val tempMap = LinkedHashMap(map)
            write(gson, getCacheFile(project, "injectorHolderMap.json"), tempMap)
            val tempClasses = LinkedHashMap(classes)
            write(gson, getCacheFile(project, "injectorHolderClasses.json"), tempClasses)
        }

        private fun setInjectTargetMethod(info: MethodInfo, holder: InjectHolder) {
            map[info] = holder
            val classPath = info.classPath
            if (classes.contains(classPath)) return
            classes[classPath] = holder
        }

        fun getByTargetMethod(info: MethodInfo): InjectHolder? {
            val holder = map[info]
            if (holder?.registerMethodInfo != null) {
                return holder
            }
            return null
        }

        fun clear(classPath: String): InjectHolder? {
            val removed = classes.remove(classPath) ?: return null
            val targetMethodInfo = removed.injectTargetMethodInfo ?: return removed
            map.remove(targetMethodInfo)
            return removed
        }

        internal fun shouldGenerateCodeInClassPath(classPath: String): Boolean {
            return classes.containsKey(classPath)
        }

    }


    internal fun setRegisterMethod(info: MethodInfo) {
        registerMethodInfo = info
    }

    internal fun setInjectTargetMethod(info: MethodInfo) {
        this.injectTargetMethodInfo = info
        setInjectTargetMethod(info, this)
    }

    fun addRegisterClass(injectInfo: ClassInfo) {
        if (classes.contains(injectInfo)) return
        classes.add(injectInfo)
    }

    internal fun injectInto(mv: MethodVisitor) {
        val registerInfo = registerMethodInfo ?: return
        val targetInfo = injectTargetMethodInfo ?: return
        if (targetInfo.isStatic) {
            if (!registerInfo.isStatic) return
        } else {
            if (registerInfo.classPath != targetInfo.classPath) return
        }

        classes.forEach {
            if (!targetInfo.isStatic) {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
            }
            mv.visitTypeInsn(Opcodes.NEW, it.classPath)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, it.classPath, "<init>", "()V", false)
            if (targetInfo.isStatic) {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    registerInfo.classPath,
                    registerInfo.name,
                    registerInfo.descriptor,
                    false
                )
            } else {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    registerInfo.classPath,
                    registerInfo.name,
                    registerInfo.descriptor,
                    false
                )
            }

        }
    }
}