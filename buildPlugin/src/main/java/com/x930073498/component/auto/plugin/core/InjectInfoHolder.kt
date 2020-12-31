package com.x930073498.component.auto.plugin.core

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 *
 */
class InjectInfoHolder {
    /**
     * key需要注入代码的filePath
     */
    private val map = mutableMapOf<String, MutableList<ScanResult>>()

    /**
     * key 为定义注解value值
     */
    private val keyMap = mutableMapOf<String, ScanResult>()

    internal fun getOrCreate(key: String): ScanResult {
        var result = keyMap[key]
        if (result == null) {
            result = ScanResult(key, this)
            keyMap[key] = result
        }
        return result
    }

    internal fun containKey(key: String): Boolean {
        return keyMap.containsKey(key)
    }

    internal fun putInjectLocation(filePath: String, result: ScanResult) {
        var list = map[filePath]
        if (list == null) {
            list = arrayListOf()
            map[filePath] = list
        }
        if (list.contains(result)) return
        list.add(result)
    }


    fun generate() {
        map.keys.forEach {
            if (it.isNotEmpty()) generateCode(it)
        }
    }

    private fun generateCode(filePath: String) {

        val resultList = map[filePath] ?: return
        if (resultList.isEmpty()) return
        val file = File(filePath)
        if (!file.exists()) return
        if (filePath.endsWith(".jar")) generateJarCode(filePath, file, resultList)
        else if (filePath.endsWith(".class")) generateClassCode(filePath, file, resultList)
    }

    private fun generateJarCode(filePath: String, file: File, list: List<ScanResult>) {

        val optJar = File(file.parent, file.name + ".opt")
        println("jarFileName=${file.name}")
        println("jarFilePath=${file.absolutePath}")
        if (optJar.exists())
            optJar.delete()
        JarFile(file).use { jar ->
            val enumeration = jar.entries()
            JarOutputStream(optJar.outputStream()).use { outputStream ->
                while (enumeration.hasMoreElements()) {
                    val jarEntry = enumeration.nextElement()
                    val entryName = jarEntry.name
                    val zipEntry = ZipEntry(entryName)
                    outputStream.putNextEntry(zipEntry)
                    jar.getInputStream(jarEntry).use { inputStream ->
                        if (entryName.endsWith(".class")) {
                            val classPath = entryName.substring(0, entryName.lastIndexOf("."))
                            val classList =
                                list.filter { it.injectLocationMethod?.classPath == classPath }
                            if (classList.isNotEmpty()) {
                                println(
                                    "generate code into:$entryName,classList=${
                                        Gson().toJson(classList)
                                    }"
                                )
                                val bytes = doGenerateCode(inputStream, classList)
                                outputStream.write(bytes)
                            } else {
                                outputStream.write(IOUtils.toByteArray(inputStream))
                            }
                        } else {
                            outputStream.write(IOUtils.toByteArray(inputStream))
                        }
                    }
                    outputStream.closeEntry()
                }
            }
        }

        if (file.exists()) {
            println("deleteResult=${file.delete()}")
        }
        println("renameResult=${optJar.renameTo(file)}")
    }


    private fun doGenerateCode(inputStream: InputStream?, list: List<ScanResult>): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = InjectClassVisitor(Opcodes.ASM6, cw, list)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    private fun generateClassCode(filePath: String, file: File, list: List<ScanResult>): ByteArray {
        val optClass = File(file.parent, file.name + ".opt")
        val inputStream = file.inputStream()
        val outputStream = optClass.outputStream()
        val bytes = doGenerateCode(inputStream, list)
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }


}