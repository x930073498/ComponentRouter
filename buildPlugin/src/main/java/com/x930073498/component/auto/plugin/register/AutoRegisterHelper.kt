package com.x930073498.component.auto.plugin.register

import com.android.builder.model.AndroidProject.FD_INTERMEDIATES
import com.google.gson.Gson
import com.x930073498.component.auto.plugin.getCacheFile
import com.x930073498.component.auto.plugin.read
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File
import java.io.FileNotFoundException

object AutoRegisterHelper {
    private const val CACHE_INFO_DIR = "auto-injector"


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
        return getCacheFile(project, "register-cache.json")

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
     * @param gson Gson
     * @return
     */
    fun readToMap(gson: Gson, file: File): HashMap<String, ScanJarHarvest> {
        return read<HashMap<String, ScanJarHarvest>>(gson, file) ?: hashMapOf()
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