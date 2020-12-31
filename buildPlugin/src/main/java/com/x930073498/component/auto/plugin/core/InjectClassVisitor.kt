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
        val result = getScanResult(name, descriptor, asIs(access, Opcodes.ACC_STATIC))
        if (result.isNotEmpty()) {
            return InjectMethodVisitor(api, mv, result)
        }
        return mv

    }

    private fun getScanResult(
        name: String?,
        descriptor: String?,
        isStatic: Boolean
    ): List<ScanResult> {
        return list.filter {
            it.injectLocationMethod?.run { this.isStatic == isStatic && this.name == name && this.descriptor == descriptor }
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
                    val classInjectorMethod = result.classInjectorMethod ?: return
                    val injectLocationMethod = result.injectLocationMethod ?: return
                    val classes = result.autoClasses
                    if (injectLocationMethod.isStatic) {
                        if (!classInjectorMethod.isStatic) return
                    } else {
                        if (classInjectorMethod.classPath != injectLocationMethod.classPath) return
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
                        if (injectLocationMethod.isStatic) {
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

