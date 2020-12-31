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
    private val key = "02472c7c-7e28-4d0f-9298-825564a5a89b"
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
                info.addAutoClass(ClassInfo(key, filePath, classPath, classPath.replace("/", ".")))
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
                info.addInjectLocation(
                    MethodInfo(
                        key,
                        filePath,
                        classPath,
                        name,
                        descriptor,
                        asIs(access, ACC_STATIC)
                    )
                )
            } else if (name == METHOD_NAME_REGISTER
                && descriptor == "(L${INTERFACE_NAME_SCAN};)V"
            ) {
                info.addClassInjector(
                    MethodInfo(
                        key, filePath, classPath, name, descriptor, asIs(
                            access,
                            ACC_STATIC
                        )
                    )
                )
            }


        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)

    }
}