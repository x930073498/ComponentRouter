package com.x930073498.plugin.register

import org.gradle.api.Project
import java.io.File

open class AutoRegisterConfig {
    val registerInfo = arrayListOf<MutableMap<String, Any>>()

    val list = arrayListOf<RegisterInfo>()

    lateinit var project: Project

    var cacheEnabled = true

    fun convertConfig() {
        registerInfo.forEach { map ->
            val info = RegisterInfo()
            info.interfaceName = map["scanInterface"]?.toString() ?: ""
            map["scanSuperClasses"].apply {
                if (this is String) {
                    info.superClassNames.add(this)
                } else if (this is List<*>) {
                    info.superClassNames.addAll(this.map { it.toString() })
                }
            }
            info.initClassName = map["codeInsertToClassName"]?.toString() ?: ""
            info.initMethodName = map["codeInsertToMethodName"]?.toString() ?: ""
            info.registerMethodName = map["registerMethodName"]?.toString() ?: ""
            info.registerClassName = map["registerClassName"]?.toString() ?: ""
            map["include"]?.apply {
                if (this is List<*>) {
                    info.include.addAll(map { it.toString() })
                }
            }

            map["exclude"]?.apply {
                if (this is List<*>) {
                    info.exclude.addAll(map { it.toString() })
                }
            }
            info.init()
            if (info.validate()) {
                list.add(info)
            } else {
                project.logger.error("auto register config error: scanInterface, codeInsertToClassName and registerMethodName should not be null\n$info")
            }
        }
        if (cacheEnabled) {
            checkRegisterInfo()
        }else {
            deleteFile(AutoRegisterHelper.getRegisterInfoCacheFile(project))
            deleteFile(AutoRegisterHelper.getRegisterCacheFile(project))
        }
    }

    private fun checkRegisterInfo() {
        val registerInfo = AutoRegisterHelper.getRegisterInfoCacheFile(project)
        val listInfo = list.toString()
        var sameInfo = false
        if (!registerInfo.exists()) {
            registerInfo.createNewFile()
        } else if (registerInfo.canRead()) {
            val info = registerInfo.readText()
            sameInfo = info == listInfo
            if (!sameInfo) {
                project.logger.error("auto-register registerInfo has been changed since project(':$project.name') last build")
            }
        } else {
            project.logger.error("auto-register read registerInfo error--------")
        }
        if (!sameInfo) {
            deleteFile(AutoRegisterHelper.getRegisterCacheFile(project))
        }
        if (registerInfo.canWrite()) {
            registerInfo.writeText(listInfo)
        } else {
            project.logger.error("auto-register write registerInfo error--------")
        }

    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun reset() {
        list.forEach {
            it.reset()
        }
    }

    override fun toString(): String {
        return buildString {
            append("autoregister")
            append("\n  cacheEnabled = ")
                .append(cacheEnabled)
                .append("\n  registerInfo = [\n")

            val size = list.size
            list.forEachIndexed { index, registerInfo ->
                append("\t").append(registerInfo.toString().replace("\n", "\n\t"))
                if (index < size - 1) {
                    append(",\n")
                }
            }
            append("\n  ]\n}")
        }
    }
}