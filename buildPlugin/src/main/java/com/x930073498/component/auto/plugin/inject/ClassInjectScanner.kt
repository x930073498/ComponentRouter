package com.x930073498.component.auto.plugin.inject

import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import com.x930073498.component.auto.plugin.core.Scanner
import org.objectweb.asm.ClassVisitor
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

object ClassInjectScanner : Scanner {
    private val pattern =
        Pattern.compile("^(android|androidx|kotlin|kotlinx|org|javax)${File.separator}.*")

    private fun match(input: CharSequence): Boolean {
        return pattern.matcher(input).find()
    }

    override fun shouldScan(
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        scanInfoHolder: ScanInfoHolder
    ): Boolean {
        if (cacheEnable) {
            return fileChanged
        }
        return true
    }

    override fun shouldScanJarFile(
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        jarFile: JarFile,
        scanInfoHolder: ScanInfoHolder
    ): Boolean {
        if (match(jarFile.name)) return false
        return true
    }

    override fun shouldScanJarEntry(
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        entryName: String,
        jarEntry: JarEntry,
        scanInfoHolder: ScanInfoHolder
    ): Boolean {
        if (match(jarEntry.name)) return false
        return true
    }

    override fun shouldScanClass(
        file: File,
        classPath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        scanInfoHolder: ScanInfoHolder
    ): Boolean {
        return true
    }

    override fun getClassVisitor(
        api: Int,
        cv: ClassVisitor,
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        scanInfoHolder: ScanInfoHolder
    ): ClassVisitor {
        return ScanClassVisitor(api, cv, filePath, scanInfoHolder)
    }
}