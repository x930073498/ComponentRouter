package com.x930073498.component.auto.plugin.inject

import com.x930073498.component.auto.plugin.asIs
import com.x930073498.component.auto.plugin.core.ClassInfo
import com.x930073498.component.auto.plugin.core.MethodInfo
import com.x930073498.component.auto.plugin.core.ScanFileInfo
import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import com.x930073498.component.auto.plugin.register.ANNOTATION_CLASS_SIGNATURE
import com.x930073498.component.auto.plugin.register.ANNOTATION_METHOD_CLASS_INJECTOR_SIGNATURE
import com.x930073498.component.auto.plugin.register.ANNOTATION_METHOD_INJECT_TO_SIGNATURE
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ScanClassVisitor(
    api: Int,
    classVisitor: ClassVisitor?,
    private val filePath: String,
    private val holder: ScanInfoHolder
) :
    ClassVisitor(api, classVisitor) {

    private val info = ScanFileInfo(filePath)
    private var isEnableClass = true
    private var classPath = filePath
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        isEnableClass = asIs(
            access,
            Opcodes.ACC_PUBLIC
        ) && !asIs(
            access,
            Opcodes.ACC_INTERFACE
        ) && !asIs(
            access,
            Opcodes.ACC_ABSTRACT
        )
        this.classPath = name.toString()
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val av = super.visitAnnotation(descriptor, visible)
        if (descriptor == ANNOTATION_CLASS_SIGNATURE && isEnableClass) {
            return AutoClassAnnotationVisitor(api, av, info, filePath, classPath)
        }
        return av
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return AnnotationMethodVisitor(api, mv, info, filePath, this.classPath, access, name, descriptor)
    }

    override fun visitEnd() {
        super.visitEnd()
        holder.flush(info)
    }


}

class InjectLocationAnnotationVisitor(
    api: Int,
    annotationVisitor: AnnotationVisitor?,
    private val info: ScanFileInfo,
    private val filePath: String,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    AnnotationVisitor(api, annotationVisitor) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        val key = value.toString()
        info.addInjectLocation(
            MethodInfo(
                key,
                filePath,
                classPath,
                this.name,
                descriptor,
                asIs(access, Opcodes.ACC_STATIC)
            )
        )
    }
}

class AnnotationMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    private val info: ScanFileInfo,
    private val filePath: String,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    MethodVisitor(api, methodVisitor) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val av = super.visitAnnotation(descriptor, visible)
        if (descriptor == ANNOTATION_METHOD_CLASS_INJECTOR_SIGNATURE) {
            return AutoClassInjectorAnnotationVisitor(
                api,
                av,
                info,
                filePath,
                classPath,
                access,
                name,
                this.descriptor
            )
        } else if (descriptor == ANNOTATION_METHOD_INJECT_TO_SIGNATURE) {
            return InjectLocationAnnotationVisitor(
                api,
                av,
                info,
                filePath,
                classPath,
                access,
                name,
                this.descriptor
            )
        }
        return av
    }

}

class AutoClassAnnotationVisitor(
    api: Int,
    annotationVisitor: AnnotationVisitor?,
    private val info: ScanFileInfo,
    private val filePath: String,
    private val classPath: String
) :
    AnnotationVisitor(api, annotationVisitor) {
    override fun visit(name: String?, value: Any?) {
        val key = value.toString()
        info.addAutoClass(ClassInfo(key, filePath, classPath, classPath.replace("/", ".")))
        super.visit(name, value)
    }
}

class AutoClassInjectorAnnotationVisitor(
    api: Int,
    annotationVisitor: AnnotationVisitor?,
    private val info: ScanFileInfo,
    private val filePath: String,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    AnnotationVisitor(api, annotationVisitor) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        val key = value.toString()
        info.addClassInjector(
            MethodInfo(
                key, filePath, classPath, this.name, descriptor,
                asIs(access, Opcodes.ACC_STATIC)
            )
        )
    }
}

