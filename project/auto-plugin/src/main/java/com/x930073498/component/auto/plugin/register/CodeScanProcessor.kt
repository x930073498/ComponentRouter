package com.x930073498.component.auto.plugin.register

import org.objectweb.asm.*
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern


internal class CodeScanProcessor(
    val info: RegisterInfo,
    private val cacheMap: MutableMap<String, ScanJarHarvest>?
) {
    private val pattern =
        Pattern.compile("^(android|androidx|kotlin|kotlinx|org|javax)${File.separator}.*")

    private fun match(input: CharSequence): Boolean {
        return pattern.matcher(input).find()
    }

    private val cachedJarContainsInitClass = mutableSetOf<String>()

    fun scanJar(jarFile: File?, destFile: File): Boolean {
        if (jarFile == null || hitCache(jarFile, destFile)) return false
        val srcFilePath = jarFile.absolutePath
        val file = JarFile(jarFile)
        val enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement() as JarEntry
            val entryName = jarEntry.name
            if (match(entryName))
                break
            checkInitClass(entryName, destFile, srcFilePath)
            if (shouldProcessClass(entryName)) {
                val inputStream = file.getInputStream(jarEntry)
                scanClass(inputStream, jarFile.absolutePath)
                inputStream.close()
            }
        }
        file.close()
        //加入缓存
        addToCacheMap(null, null, srcFilePath)
        return true
    }

    private fun hitCache(jarFile: File, destFile: File): Boolean {
        val jarFilePath = jarFile.absolutePath
        if (cacheMap != null) {
            val scanJarHarvest = cacheMap[jarFilePath]
            if (scanJarHarvest != null) {
                val info = info
                scanJarHarvest.harvestList.forEach { harvest ->
                    if (harvest.isInitClass) {
                        if (CLASS_NAME_CODE_INSERT_TO == harvest.className) {
                            info.fileContainsInitClass = destFile
                            cachedJarContainsInitClass.add(jarFilePath)
                        }
                    } else if (INTERFACE_NAME_SCAN == harvest.interfaceName) {
                        info.classList.add(harvest.className)
                    }

                }
                return true
            }
        }
        return false
    }

    fun checkInitClass(entryName: String?, destFile: File): Boolean {
        return checkInitClass(entryName, destFile, "")
    }

    private fun checkInitClass(entryName: String?, destFile: File, srcFilePath: String): Boolean {
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        var found = false
        if (NAME_CODE_INSERT_TO_CLASS == entryName) {
            info.fileContainsInitClass = destFile
            if (destFile.name.endsWith(".jar")) {
                addToCacheMap(null, CLASS_NAME_CODE_INSERT_TO, srcFilePath)
                found = true
            }
        }

        return found
    }

    private fun addToCacheMap(interfaceName: String?, name: String?, srcFilePath: String) {
        if (!srcFilePath.endsWith(".jar") || cacheMap == null) return
        var jarHarvest = cacheMap[srcFilePath]
        if (jarHarvest == null) {
            jarHarvest = ScanJarHarvest(arrayListOf())
            cacheMap[srcFilePath] = jarHarvest
        }
        if (!name.isNullOrEmpty()) {
            val classInfo = Harvest(name, interfaceName, interfaceName == null)
            jarHarvest.harvestList.add(classInfo)
        }
    }

    fun shouldProcessClass(entryName: String?): Boolean {
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        val name = entryName.substring(0, entryName.lastIndexOf('.'))
        if (shouldProcessThisClassForRegister(info, name)) {
            return true
        }
        return false

    }

    fun isCachedJarContainsInitClass(filePath: String): Boolean {
        return cachedJarContainsInitClass.contains(filePath)
    }

    fun scanClass(file: File?): Boolean {
        return file != null && scanClass(file.inputStream(), file.absolutePath)
    }

    private fun scanClass(inputStream: InputStream, filePath: String): Boolean {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = ScanClassVisitor(Opcodes.ASM6, cw, filePath)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
        return cv.isFound()
    }

    companion object {
        fun shouldProcessThisClassForRegister(info: RegisterInfo?, entryName: String?): Boolean {
            if (entryName.isNullOrEmpty()) return false
            if (info != null) {
                val list = info.includePatterns
                val excludeList = info.excludePatterns
                list.forEach { pattern ->
                    if (pattern.matcher(entryName).matches()) {
                        excludeList.forEach { p ->
                            if (p.matcher(entryName).matches())
                                return false
                        }
                        return true
                    }
                }
            }
            return false
        }
    }

    inner class ScanClassVisitor(
        api: Int,
        classVisitor: ClassVisitor?,
        private val filePath: String
    ) :
        ClassVisitor(api, classVisitor) {
        private var found = false

        private fun asIs(access: Int, flag: Int): Boolean {
            return access and flag == flag
        }

        fun isFound(): Boolean {
            return found
        }


        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, name, signature, superName, interfaces)
            if (asIs(access, Opcodes.ACC_ABSTRACT)
                || asIs(access, Opcodes.ACC_INTERFACE)
                || !asIs(access, Opcodes.ACC_PUBLIC)
            ) {
                return
            }
            val ext = info


            if (shouldProcessThisClassForRegister(ext, name)) {
                val scanInterfaceName = interfaces?.firstOrNull { it == INTERFACE_NAME_SCAN }
                if (scanInterfaceName != null) {
                    if (!ext.classList.contains(name))
                        ext.classList.add(name)//需要把对象注入到管理类  就是fileContainsInitClass
                    addToCacheMap(scanInterfaceName, name, filePath)
                    found = true
                }

            }
        }


    }
}