package com.x930073498.component.auto.plugin.register

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


internal class CodeInsertProcessor(private val extension: RegisterInfo) {
    companion object {
        fun insertInitCodeTo(extension: RegisterInfo?) {
            if (extension != null && extension.classList.isNotEmpty()) {
                val processor = CodeInsertProcessor(extension)
                val file = extension.fileContainsInitClass ?: return
                if (file.name.endsWith(".jar"))
                    processor.generateCodeIntoJarFile(file)
                else
                    processor.generateCodeIntoClassFile(file)
            }
        }
    }

    fun generateCodeIntoJarFile(jarFile: File?): File? {
        if (jarFile != null) {
            val optJar = File(jarFile.parent, jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            val file = JarFile(jarFile)
            val enumeration = file.entries()
            val jarOutputStream = JarOutputStream(optJar.outputStream())
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (isInitClass(entryName)) {
                    println("generate code into:$entryName")
                    val bytes = doGenerateCode(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()
            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

   private fun isInitClass(entryName: String?): Boolean {
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        return NAME_CODE_INSERT_TO_CLASS == entryName
    }

    fun generateCodeIntoClassFile(file: File): ByteArray {
        val optClass = File(file.parent, file.name + ".opt")
        val inputStream = file.inputStream()
        val outputStream = optClass.outputStream()
        val bytes = doGenerateCode(inputStream)
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }

   private fun doGenerateCode(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = MyClassVisitor(Opcodes.ASM6, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    inner class MyClassVisitor(api: Int, classVisitor: ClassVisitor?) :
        ClassVisitor(api, classVisitor) {

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (name == METHOD_NAME_CODE_INSERT_TO) {
                mv = MyMethodVisitor(Opcodes.ASM6, mv, (access and Opcodes.ACC_STATIC) > 0)
            }
            return mv
        }
    }

    inner class MyMethodVisitor(
        api: Int,
        methodVisitor: MethodVisitor?,
        private val _static: Boolean
    ) :
        MethodVisitor(api, methodVisitor) {
        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                extension.classList.forEach { name ->
                    if (!_static) {
                        //加载this
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                    }
                    mv.visitTypeInsn(Opcodes.NEW, name)
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
                    //调用注册方法将组件实例注册到组件库中
                    if (_static) {
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            CLASS_NAME_CODE_INSERT_TO,
                            METHOD_NAME_REGISTER,
                            "(L$INTERFACE_NAME_SCAN;)V",
                            false
                        )
                    } else {
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            CLASS_NAME_CODE_INSERT_TO,
                            METHOD_NAME_REGISTER,
                            "(L$INTERFACE_NAME_SCAN;)V",
                            false
                        )
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