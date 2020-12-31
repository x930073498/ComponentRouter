package com.x930073498.component.auto.plugin.core

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile


fun File.isJar(): Boolean {
    return absolutePath.endsWith(".jar")
}

fun File.isClass(): Boolean {
    return absolutePath.endsWith(".class")
}


class FileScan {
    private val scanners = arrayListOf<Scanner>()


    fun addScanner(scanner: Scanner) {
        if (scanners.contains(scanner)) return
        scanners.add(scanner)
    }

    fun scan(file: File, holder: ScanInfoHolder, fileChanged: Boolean, cacheEnable: Boolean) {
        val filePath = file.absolutePath
        val list =
            scanners.filter { it.shouldScan(file, filePath, fileChanged, cacheEnable, holder) }
        if (file.isJar()) {
            scanJar(list, holder, file, filePath, fileChanged, cacheEnable)
        } else if (file.isClass()) {
            scanClass(list, holder, file, filePath, fileChanged, cacheEnable)
        }

    }

    private fun scanJar(
        scanners: List<Scanner>,
        holder: ScanInfoHolder,
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean
    ) {
        if (scanners.isEmpty()) return
        JarFile(file).use { jar ->
            val list = scanners.filter {
                it.shouldScanJarFile(file, filePath, fileChanged, cacheEnable, jar, holder)
            }
            if (list.isEmpty()) return
            val enumeration = jar.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement() as JarEntry
                val entryName = jarEntry.name
                if (entryName == null || !entryName.endsWith(".class")) {
                    continue
                }
                val jarEntryScanners = list.filter {
                    it.shouldScanJarEntry(
                        file,
                        filePath,
                        fileChanged,
                        cacheEnable,
                        entryName,
                        jarEntry,
                        holder
                    )
                }
                jar.getInputStream(jarEntry).use {
                    scanInputStream(
                        jarEntryScanners,
                        holder,
                        it,
                        file,
                        filePath,
                        fileChanged,
                        cacheEnable
                    )
                }

            }
        }
    }

    private fun scanInputStream(
        scanners: List<Scanner>,
        holder: ScanInfoHolder,
        inputStream: InputStream,
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean
    ) {
        if (scanners.isEmpty()) {
            return
        }
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        var cv: ClassVisitor = cw
        scanners.forEach {
            cv = it.getClassVisitor(
                Opcodes.ASM6,
                cv,
                file,
                filePath,
                fileChanged,
                cacheEnable,
                holder
            )
        }
        if (cv == cw) {
            return
        }
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
    }

    private fun scanClass(
        scanners: List<Scanner>,
        holder: ScanInfoHolder,
        file: File,
        classPath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean
    ) {
        if (scanners.isEmpty()) return
        val list = scanners.filter {
            it.shouldScanClass(
                file,
                classPath,
                fileChanged,
                cacheEnable,
                holder
            )
        }

        file.inputStream().use {
            scanInputStream(list, holder, it, file, classPath, fileChanged, cacheEnable)
        }
    }
}