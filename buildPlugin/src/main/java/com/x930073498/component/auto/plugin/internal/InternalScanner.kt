package com.x930073498.component.auto.plugin.internal

import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import com.x930073498.component.auto.plugin.core.Scanner
import org.objectweb.asm.ClassVisitor
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

object InternalScanner : Scanner {


    override fun shouldScan(
        file: File,
        filePath: String,
        fileChanged: Boolean,
        cacheEnable: Boolean,
        scanInfoHolder: ScanInfoHolder
    ): Boolean {
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
        return InternalClassVisitor(api,cv,filePath,scanInfoHolder)
    }
}