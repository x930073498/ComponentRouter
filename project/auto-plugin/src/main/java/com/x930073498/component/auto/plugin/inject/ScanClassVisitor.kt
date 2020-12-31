package com.x930073498.component.auto.plugin.inject

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ScanClassVisitor(api: Int, classVisitor: ClassVisitor?, private val classPath: String) :
    ClassVisitor(api, classVisitor) {


    private var isEnableClass = true
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        isEnableClass = asIs(access, Opcodes.ACC_PUBLIC) && !asIs(access, Opcodes.ACC_INTERFACE) && !asIs(
            access,
            Opcodes.ACC_ABSTRACT
        )
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val av = super.visitAnnotation(descriptor, visible)
        if (descriptor == ANNOTATION_CLASS_SIGNATURE && isEnableClass) {
            return AutoClassAnnotationVisitor(api, av, classPath)
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
        return AnnotationMethodVisitor(api, mv, classPath, access, name, descriptor)
    }


}
class InjectLocationAnnotationVisitor(
    api: Int,
    annotationVisitor: AnnotationVisitor?,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    AnnotationVisitor(api, annotationVisitor) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        Injector.setInjectTargetMethod(
            value.toString(),
            MethodInfo(classPath, this.name, descriptor, asIs(access, Opcodes.ACC_STATIC))
        )
    }
}

class AnnotationMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    MethodVisitor(api, methodVisitor) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val av = super.visitAnnotation(descriptor, visible)
        if (descriptor == ANNOTATION_METHOD_CLASS_INJECTOR_SIGNATURE) {
            return AutoClassInjectorAnnotationVisitor(api, av, classPath, access, name, this.descriptor)
        } else if (descriptor == ANNOTATION_METHOD_INJECT_TO_SIGNATURE) {
            return InjectLocationAnnotationVisitor(api, av, classPath, access, name, this.descriptor)
        }
        return av
    }

}
class AutoClassAnnotationVisitor(api: Int, annotationVisitor: AnnotationVisitor?, private val classPath: String) :
    AnnotationVisitor(api, annotationVisitor) {
    override fun visit(name: String?, value: Any?) {
        val key = value.toString()
        Injector.addRegisterClass(key, ClassInfo(classPath, classPath.replace("/", ".")))
        super.visit(name, value)
    }
}

class AutoClassInjectorAnnotationVisitor(
    api: Int,
    annotationVisitor: AnnotationVisitor?,
    private val classPath: String,
    private val access: Int,
    private val name: String,
    private val descriptor: String
) :
    AnnotationVisitor(api, annotationVisitor) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        Injector.setRegisterMethod(
            value.toString(),
            MethodInfo(classPath, this.name, descriptor, asIs(access, Opcodes.ACC_STATIC))
        )
    }
}

