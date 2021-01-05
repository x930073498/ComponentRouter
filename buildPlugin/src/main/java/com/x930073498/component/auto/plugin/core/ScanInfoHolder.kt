package com.x930073498.component.auto.plugin.core

import com.google.gson.Gson
import com.x930073498.component.auto.plugin.getCacheFile
import org.gradle.api.Project
import com.x930073498.component.auto.plugin.*

class ScanInfoHolder(private val project: Project) {

    private val injectHolder: InjectInfoHolder = InjectInfoHolder()
    private val scanPathMap = mutableMapOf<String, ScanFileInfo>()
    private val cacheFile by lazy {
        getCacheFile(project, "scanInfoHolderMap.json")
    }
    private val gson = Gson()


    fun isEmpty(): Boolean {
        return scanPathMap.isEmpty() || scanPathMap.values.firstOrNull { it.isNotEmpty() } == null
    }

    fun loadCache() {
        val map = read<HashMap<String, ScanFileInfo>>(gson, cacheFile) ?: return
        scanPathMap.clear()
        scanPathMap.putAll(map)
        transformToInjectInfoHolder()
    }

    fun saveCache() {
        write(gson, cacheFile, HashMap<String, ScanFileInfo>(scanPathMap))
    }


    fun hasInjectLocationMethod(key: String): Boolean {
        val result = injectHolder.get(key) ?: return false
        return result.injectLocationMethod != null
    }

    fun removeInjectLocationMethod(key: String) {
        val result = injectHolder.get(key) ?: return
        result.removeInjectLocationMethod()
    }

    fun hasClassInjectorMethod(key: String): Boolean {
        val result = injectHolder.get(key) ?: return false
        return result.classInjectorMethod != null
    }

    fun removeClassInjectorMethod(key: String) {
        val result = injectHolder.get(key) ?: return
        result.removeClassInjectorMethod()
    }

    fun removeAutoClass(classInfo: ClassInfo) {
        val result = injectHolder.get(classInfo.key) ?: return
        result.removeAutoClass(classInfo)
    }

    fun hasInfo(filePath: String): Boolean {
        return scanPathMap[filePath]?.isNotEmpty() == true
    }

    fun clearCache() {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    private fun push(scanFileInfo: ScanFileInfo) {
        val filePath = scanFileInfo.filePath
        var info = scanPathMap[filePath]
        if (info == null) {
            info = ScanFileInfo(filePath)
            scanPathMap[filePath] = info
        }
        info.push(scanFileInfo)
    }

    internal fun flush(scanFileInfo: ScanFileInfo) {
        addInjectInfo(scanFileInfo)
        push(scanFileInfo)
    }

    fun containKey(key: String): Boolean {
        return injectHolder.containKey(key)
    }


    fun generate() {
        injectHolder.generate()
    }

    fun removeFilePath(filePath: String) {
        val scanFileInfo = scanPathMap.remove(filePath) ?: return
        scanFileInfo.injectLocationMethods.forEach {
            removeInjectLocationMethod(it.key)
        }
        scanFileInfo.classInjectorMethods.forEach {
            removeClassInjectorMethod(it.key)
        }
        scanFileInfo.autoClasses.forEach {
            removeAutoClass(it)
        }
    }

    private fun transformToInjectInfoHolder() {
        scanPathMap.values.forEach { scan ->
            addInjectInfo(scan)
        }
    }

    private fun addInjectInfo(scan: ScanFileInfo) {
        scan.autoClasses.forEach {
            injectHolder.getOrCreate(it.key).addAutoClass(it)
        }
        scan.classInjectorMethods.forEach {
            injectHolder.getOrCreate(it.key).setClassInjectorMethod(it)
        }
        scan.injectLocationMethods.forEach {
            injectHolder.getOrCreate(it.key).setInjectLocationMethod(it)
        }
    }
}