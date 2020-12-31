package com.x930073498.component.auto.plugin.inject

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InjectClassVisitor(api: Int, classVisitor: ClassVisitor?, private val classPath: String) :
    ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return Injector.inject(mv, classPath, name, descriptor)
    }


}
class InjectMethodVisitor(api: Int, methodVisitor: MethodVisitor?, private val holder: InjectHolder) :
    MethodVisitor(api, methodVisitor) {

    override fun visitInsn(opcode: Int) {
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            holder.injectInto(mv)
        }
        super.visitInsn(opcode)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack + 4, maxLocals)
    }
}