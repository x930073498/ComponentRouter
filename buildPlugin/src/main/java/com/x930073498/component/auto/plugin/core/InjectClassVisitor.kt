package com.x930073498.component.auto.plugin.core

import com.x930073498.component.auto.plugin.asIs
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InjectClassVisitor(
    api: Int,
    classVisitor: ClassVisitor?,
    private val list: List<ScanResult>
) : ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        val result = getScanResult(name, descriptor, access)
        if (result.isNotEmpty()) {
            return InjectMethodVisitor(api, mv, result)
        }
        return mv

    }

    private fun getScanResult(
        name: String?,
        descriptor: String?,
        access: Int
    ): List<ScanResult> {
        return list.filter {
            it.injectLocationMethod?.run { this.access == access && this.name == name && this.descriptor == descriptor }
                ?: false
        }
    }

    class InjectMethodVisitor(
        api: Int,
        methodVisitor: MethodVisitor?,
        private val list: List<ScanResult>
    ) :
        MethodVisitor(api, methodVisitor) {
        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                list.forEach { result ->
                    val classInjectorMethod = result.classInjectorMethod ?: return@forEach
                    val injectLocationMethod = result.injectLocationMethod ?: return@forEach
                    val classes = result.autoClasses

                    if (!classInjectorMethod.isStatic) {
                        //不是静态方法
                        if (classInjectorMethod.classPath != injectLocationMethod.classPath) return@forEach
                    } else {
                        //静态方法
                        if (classInjectorMethod.isProtected) {
                            if (classInjectorMethod.getPackageName() != injectLocationMethod.getPackageName()) return@forEach
                        } else if (classInjectorMethod.isPrivate) {
                            if (classInjectorMethod.classPath != injectLocationMethod.classPath) return@forEach
                        }
                    }
                    classes.forEach {
                        if (!injectLocationMethod.isStatic) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0)
                        }
                        mv.visitTypeInsn(Opcodes.NEW, it.classPath)
                        mv.visitInsn(Opcodes.DUP)
                        mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL,
                            it.classPath,
                            "<init>",
                            "()V",
                            false
                        )
                        if (classInjectorMethod.isStatic) {
                            mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                classInjectorMethod.classPath,
                                classInjectorMethod.name,
                                classInjectorMethod.descriptor,
                                false
                            )
                        } else {
                            mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                classInjectorMethod.classPath,
                                classInjectorMethod.name,
                                classInjectorMethod.descriptor,
                                false
                            )
                        }
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }

}

