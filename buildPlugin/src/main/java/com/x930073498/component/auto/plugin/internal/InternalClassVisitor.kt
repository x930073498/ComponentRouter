package com.x930073498.component.auto.plugin.internal

import com.x930073498.component.auto.plugin.asIs
import com.x930073498.component.auto.plugin.core.ClassInfo
import com.x930073498.component.auto.plugin.core.MethodInfo
import com.x930073498.component.auto.plugin.core.ScanFileInfo
import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import com.x930073498.component.auto.plugin.register.*
import groovyjarjarasm.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InternalClassVisitor(
    api: Int, classVisitor: ClassVisitor?,
    private val filePath: String,
    private val holder: ScanInfoHolder
) :
    ClassVisitor(api, classVisitor) {

    private val info = ScanFileInfo(filePath)
    private var classPath = filePath
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        classPath = name ?: classPath
        super.visit(version, access, name, signature, superName, interfaces)
        if (asIs(access, Opcodes.ACC_ABSTRACT)
            || asIs(access, Opcodes.ACC_INTERFACE)
            || !asIs(access, Opcodes.ACC_PUBLIC)
        ) return

        if (interfaces != null) {
            if (interfaces.contains(INTERFACE_NAME_SCAN)) {
                info.addAutoClass(
                    ClassInfo(
                        INTERNAL_SCANNER_KEY,
                        filePath,
                        classPath,
                        classPath.replace("/", ".")
                    )
                )
            }
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        holder.flush(info)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (classPath == NAME_CODE_INSERT_TO_CLASS_PATH) {
            //"load"  location
            if (name == METHOD_NAME_CODE_INSERT_TO && descriptor == "()V") {
                if (holder.hasInjectLocationMethod(INTERNAL_SCANNER_KEY)) {
                    holder.removeInjectLocationMethod(INTERNAL_SCANNER_KEY)
                }
                info.addInjectLocation(
                    MethodInfo(
                        INTERNAL_SCANNER_KEY,
                        filePath,
                        classPath,
                        name,
                        descriptor,
                        access
                    )
                )
            } else if (name == METHOD_NAME_REGISTER
                && descriptor == "(L${INTERFACE_NAME_SCAN};)V"
            ) {
                if (holder.hasClassInjectorMethod(INTERNAL_SCANNER_KEY)) {
                    holder.removeClassInjectorMethod(INTERNAL_SCANNER_KEY)
                }
                info.addClassInjector(
                    MethodInfo(
                        INTERNAL_SCANNER_KEY, filePath, classPath, name, descriptor, access
                    )
                )
            }


        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)

    }
}