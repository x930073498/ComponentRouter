package com.x930073498.component.auto.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.x930073498.component.auto.plugin.inject.ClassInjectScanner
import com.x930073498.component.auto.plugin.core.FileScan
import com.x930073498.component.auto.plugin.core.ScanInfoHolder
import com.x930073498.component.auto.plugin.internal.InternalScanner
import com.x930073498.component.auto.plugin.register.eachFileRecurse
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File

class ASMTransform constructor(
    val project: Project
) : Transform() {


    override fun getName(): String {
        return "auto-injector"
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

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        if (transformInvocation == null) return
        val time = System.currentTimeMillis()
        val clearCache = !transformInvocation.isIncremental
        if (clearCache) {
            transformInvocation.outputProvider.deleteAll()
        }
        val cacheEnable = transformInvocation.isIncremental
        val holder = ScanInfoHolder(project)
        val fileScan = FileScan()
        fileScan.addScanner(InternalScanner)
        fileScan.addScanner(ClassInjectScanner)
        if (cacheEnable) {
            holder.loadCache()
        } else {
            holder.clearCache()
        }
//        scanWithCopy(transformInvocation, holder, fileScan, cacheEnable,time)
        scan(transformInvocation, holder, fileScan, cacheEnable, time)
        val scanFinishTime = System.currentTimeMillis()
        project.logger.error("injector scan all class cost time: " + (scanFinishTime - time) + " ms")

        holder.generate()
        val insertTime = System.currentTimeMillis()
        project.logger.error("injector insert code cost time: " + (insertTime - scanFinishTime) + " ms")
        copy(transformInvocation)
        val copyTime = System.currentTimeMillis()
        project.logger.error("injector copy code cost time: " + (copyTime - insertTime) + " ms")
        holder.saveCache()
        val finishTime = System.currentTimeMillis()
        project.logger.error("injector cost time: " + (finishTime - time) + " ms")
    }


    private fun copy(transformInvocation: TransformInvocation) {
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                val dest = getDestFile(jarInput, transformInvocation.outputProvider)
                val src = jarInput.file
                FileUtils.copyFile(src, dest)
            }

            input.directoryInputs.forEach { directoryInput ->
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }

    private fun scanWithCopy(
        transformInvocation: TransformInvocation,
        holder: ScanInfoHolder,
        fileScan: FileScan,
        cacheEnable: Boolean,
        time: Long
    ) {
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                val dest = getDestFile(jarInput, transformInvocation.outputProvider)
                val src = jarInput.file
                FileUtils.copyFile(src, dest)
                if (jarInput.status != Status.NOTCHANGED) {
                    fileScan.scan(
                        dest,
                        holder,
                        true,
                        cacheEnable
                    )
                } else {
                    fileScan.scan(
                        dest,
                        holder,
                        false,
                        cacheEnable
                    )
                }
                project.logger.info("auto-injector cost time: " + (System.currentTimeMillis() - time) + " ms to scan jar file:" + dest.absolutePath)
            }

            input.directoryInputs.forEach { directoryInput ->
                val dirTime = System.currentTimeMillis()
                var root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                val changeMap = directoryInput.changedFiles
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
                dest.eachFileRecurse { file ->
                    if (file.isFile) {
                        fileScan.scan(
                            file,
                            holder,
                            true,
                            cacheEnable
                        )
                    }
                }
                val scanTime = System.currentTimeMillis()
                project.logger.info("auto-injector cost time: ${System.currentTimeMillis() - time}, scan time: ${scanTime - dirTime}. path=${root}")
            }
        }

    }

    private fun scan(
        transformInvocation: TransformInvocation,
        holder: ScanInfoHolder,
        fileScan: FileScan,
        cacheEnable: Boolean,
        time: Long
    ) {
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                val scanStartTime = System.currentTimeMillis()
                val src = jarInput.file
                if (jarInput.status != Status.NOTCHANGED) {
                    fileScan.scan(
                        src,
                        holder,
                        true,
                        cacheEnable
                    )
                } else {
                    fileScan.scan(
                        src,
                        holder,
                        false,
                        cacheEnable
                    )
                }
                val scanEndTime = System.currentTimeMillis()
                project.logger.info("auto-injector cost time: ${scanEndTime - time}, scan time: ${scanEndTime - scanStartTime}. path=${src.absolutePath}")
            }

            input.directoryInputs.forEach { directoryInput ->
                val dirTime = System.currentTimeMillis()
                var root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                val changeMap = directoryInput.changedFiles
                directoryInput.file.eachFileRecurse { file ->
                    if (file.isFile) {
                        fileScan.scan(
                            file,
                            holder,
                            changeMap[file] != Status.NOTCHANGED,
                            cacheEnable
                        )
                    }
                }
                val scanTime = System.currentTimeMillis()
                project.logger.info("auto-injector cost time: ${System.currentTimeMillis() - time}, scan time: ${scanTime - dirTime}. path=${root}")
            }
        }

    }

}