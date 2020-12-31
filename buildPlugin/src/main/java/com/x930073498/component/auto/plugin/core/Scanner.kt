package com.x930073498.component.auto.plugin.core

import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import org.objectweb.asm.ClassVisitor
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 自定义扫描器
 */
interface Scanner {

    /**
     * 是否扫描这个文件
     */
    fun shouldScan(file: File, filePath: String,fileChanged:Boolean,cacheEnable:Boolean, scanInfoHolder: ScanInfoHolder): Boolean


    /**
     * 是否扫描这个jarfile
     */
    fun shouldScanJarFile(
        file: File,
        filePath: String,
        fileChanged:Boolean,
        cacheEnable:Boolean,
        jarFile: JarFile,
        scanInfoHolder: ScanInfoHolder
    ):Boolean

    /**
     *是否扫描这个jarEntry
     */
    fun shouldScanJarEntry(
        file: File,
        filePath: String,
        fileChanged:Boolean,
        cacheEnable:Boolean,
        entryName: String,
        jarEntry: JarEntry,
        scanInfoHolder: ScanInfoHolder
    ): Boolean

    /**
     * 是否扫描这个class
     */
    fun shouldScanClass(
        file: File,
        classPath: String,
        fileChanged:Boolean,
        cacheEnable:Boolean,
        scanInfoHolder: ScanInfoHolder
    ): Boolean

    fun getClassVisitor(
        api: Int,
        cv: ClassVisitor,
        file: File,
        filePath: String,
        fileChanged:Boolean,
        cacheEnable:Boolean,
        scanInfoHolder: ScanInfoHolder
    ): ClassVisitor

}