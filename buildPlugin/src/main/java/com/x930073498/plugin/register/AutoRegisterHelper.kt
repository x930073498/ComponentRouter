package com.x930073498.plugin.register

import com.android.builder.model.AndroidProject.FD_INTERMEDIATES
import com.google.gson.Gson
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Type

object AutoRegisterHelper {
    val CACHE_INFO_DIR = "auto-register"


    /**
     * 缓存自动注册配置的文件
     * @param project
     * @return file
     */
    fun getRegisterInfoCacheFile(project: Project): File {
        val baseDir = getCacheFileDir(project)
        if (mkdirs(baseDir)) {
            return File(baseDir + "register-info.config")
        } else {
            throw  FileNotFoundException("Not found  path:$baseDir")
        }
    }

    /**
     * 缓存扫描到结果的文件
     * @param project
     * @return File
     */
    fun getRegisterCacheFile(project: Project): File {
        val baseDir = getCacheFileDir(project)
        if (mkdirs(baseDir)) {
            return File(baseDir + "register-cache.json")
        } else {
            throw  FileNotFoundException("Not found  path:$baseDir")
        }
    }

    /**
     * 将扫描到的结果缓存起来
     * @param cacheFile
     * @param harvests
     */
    fun cacheRegisterHarvest(cacheFile: File?, harvests: String?) {
        if (cacheFile == null || harvests.isNullOrEmpty()) return
        cacheFile.ensureParentDirsCreated()
        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
        }
        cacheFile.writeText(harvests)
    }

    fun getCacheFileDir(project: Project): String {
        return project.run {
            buildDir.absolutePath + File.separator + FD_INTERMEDIATES + File.separator + CACHE_INFO_DIR + File.separator
        }
    }

    /**
     * 读取文件内容并创建Map
     * @param file 缓存文件
     * @param type map的类型
     * @return
     */
    fun <T> readToMap(file: File, type: Type?): T? {
        return if (file.exists()) {
            if (type != null) {
                val text = file.readText()
                if (text.isNotEmpty()) {
                    Gson().fromJson(text, type)
                } else {
                    null
                }
            } else {
                null
            }
        } else null
    }

    fun mkdirs(path: String): Boolean {
        val baseDirFile = File(path)
        var result = true
        if (!baseDirFile.isDirectory) {
            result = baseDirFile.mkdirs()
        }
        return result

    }

}