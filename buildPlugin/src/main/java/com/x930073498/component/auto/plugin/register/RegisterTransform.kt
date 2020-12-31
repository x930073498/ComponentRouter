package com.x930073498.component.auto.plugin.register

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.io.FileNotFoundException

internal class RegisterTransform constructor(
     val project: Project
) : Transform() {

    lateinit var config: AutoRegisterConfig
    override fun getName(): String {
        return "auto-register"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation) {
        project.logger.warn("start auto-register transform...")
        config.reset()
        project.logger.warn(config.toString())
        val clearCache = !transformInvocation.isIncremental
        if (clearCache) {
            transformInvocation.outputProvider.deleteAll()
        }
        val time = System.currentTimeMillis()
        val leftSlash = File.separator == "/"
        val cacheEnabled = config.cacheEnabled
        println("auto-register-----------isIncremental:${isIncremental}--------config.cacheEnabled:${cacheEnabled}--------------------\n")

        var cacheMap: HashMap<String, ScanJarHarvest>? = null
        var gson: Gson? = null
        var cacheFile: File? = null
        if (cacheEnabled) {
            gson = Gson()
            cacheFile = AutoRegisterHelper.getRegisterCacheFile(project)
            if (clearCache && cacheFile.exists())
                cacheFile.delete()
            cacheMap = AutoRegisterHelper.readToMap(gson, cacheFile)
        }
        val scanProcessor = CodeScanProcessor(config.info, cacheMap)
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                println("path=${jarInput.file.absolutePath}")
                if (jarInput.status != Status.NOTCHANGED && cacheMap != null) {
                    cacheMap.remove(jarInput.file.absolutePath)
                }
                scanJar(jarInput, transformInvocation.outputProvider, scanProcessor)
            }
            input.directoryInputs.forEach { directoryInput ->
                val dirTime = System.currentTimeMillis()
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                var root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                directoryInput.file.eachFileRecurse { file ->
                    val path = file.absolutePath.replace(root, "")
                    if (file.isFile) {
                        var entryName = path
                        if (!leftSlash) {
                            entryName = entryName.replace("\\\\", "/")
                        }
                        scanProcessor.checkInitClass(
                            entryName,
                            File(dest.absolutePath + File.separator + path)
                        )
                        if (scanProcessor.shouldProcessClass(entryName)) {
                            scanProcessor.scanClass(file)
                        }

                    }
                }
                val scanTime = System.currentTimeMillis()
                FileUtils.copyDirectory(directoryInput.file, dest)
                println("auto-register cost time: ${System.currentTimeMillis() - dirTime}, scan time: ${scanTime - dirTime}. path=${root}")
            }
        }

        if (cacheMap != null && cacheFile != null && gson != null) {
            val json = gson.toJson(cacheMap)
            AutoRegisterHelper.cacheRegisterHarvest(cacheFile, json)
        }
        val scanFinishTime = System.currentTimeMillis()
        project.logger.error("register scan all class cost time: " + (scanFinishTime - time) + " ms")
        val ext = config.info
        if (ext.fileContainsInitClass != null) {
            println("insert register code to file:" + ext.fileContainsInitClass?.absolutePath)
            if (ext.classList.isEmpty()) {
                project.logger.error("No class implements found for interface:$INTERFACE_NAME_SCAN")
            } else {
                ext.classList.forEach {
                    println(it)
                }
                CodeInsertProcessor.insertInitCodeTo(ext)
            }
        } else {
            project.logger.error("The specified register class not found:$CLASS_NAME_CODE_INSERT_TO")
        }
        val finishTime = System.currentTimeMillis()
        project.logger.error("register insert code cost time: " + (finishTime - scanFinishTime) + " ms")
        project.logger.error("register cost time: " + (finishTime - time) + " ms")
    }

    private fun scanJar(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        scanProcessor: CodeScanProcessor
    ) {
        val src = jarInput.file
        val dest = getDestFile(jarInput, outputProvider)
        val time = System.currentTimeMillis()
        if (!scanProcessor.scanJar(src, dest) //直接读取了缓存，没有执行实际的扫描
            //此jar文件中不需要被注入代码
            //为了避免增量编译时代码注入重复，被注入代码的jar包每次都重新复制
            && !scanProcessor.isCachedJarContainsInitClass(src.absolutePath)
        ) {
            //不需要执行文件复制，直接返回
            return
        }
        println("auto-register cost time: " + (System.currentTimeMillis() - time) + " ms to scan jar file:" + dest.absolutePath)
        FileUtils.copyFile(src, dest)
    }

    companion object {
        fun getDestFile(jarInput: JarInput, outputProvider: TransformOutputProvider): File {
            var destName = jarInput.name
            val hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (destName.endsWith(".jar")) {
                destName = destName.substring(0, destName.length - 4)
            }
            return outputProvider.getContentLocation(
                destName + "_" + hexName,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR
            )
        }
    }
}


fun File.eachFileRecurse(action: (File) -> Unit) {
    eachFileRecurse(FileType.ANY, action)
}

fun File.eachFileRecurse(

    fileType: FileType,
    closure: (File) -> Unit
) {
    checkDir(this)
    val files = this.listFiles()
    if (files != null) {
        val var5 = files.size
        for (var6 in 0 until var5) {
            val file = files[var6]
            if (file.isDirectory) {
                if (fileType != FileType.FILES) {
                    closure.invoke(file)
                }
                file.eachFileRecurse(fileType, closure)
            } else if (fileType != FileType.DIRECTORIES) {
                closure.invoke(file)
            }
        }
    }
}

private fun checkDir(dir: File) {
    if (!dir.exists()) throw FileNotFoundException(dir.absolutePath)
    require(dir.isDirectory) { "The provided File object is not a directory: " + dir.absolutePath }
}