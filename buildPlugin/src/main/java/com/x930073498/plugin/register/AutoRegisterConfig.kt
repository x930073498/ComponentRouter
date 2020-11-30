package com.x930073498.plugin.register

import org.gradle.api.Project
import java.io.File

open class AutoRegisterConfig {

    val info = RegisterInfo()

    lateinit var project: Project

    var cacheEnabled = true

    fun convertConfig() {
        info.interfaceName = INTERFACE_NAME_SCAN
        info.initClassName = CLASS_NAME_CODE_INSERT_TO
        info.initMethodName = METHOD_NAME_CODE_INSERT_TO
        info.registerMethodName = METHOD_NAME_REGISTER
        info.init()
        if (cacheEnabled) {
            checkRegisterInfo()
        } else {
            deleteFile(AutoRegisterHelper.getRegisterInfoCacheFile(project))
            deleteFile(AutoRegisterHelper.getRegisterCacheFile(project))
        }
    }

    private fun checkRegisterInfo() {
        val registerInfo = AutoRegisterHelper.getRegisterInfoCacheFile(project)
        val listInfo = info.toString()
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
        info.reset()
    }

    override fun toString(): String {
        return buildString {
            append("autoregister")
            append("\n  cacheEnabled = ")
                .append(cacheEnabled)
                .append("\n  registerInfo = \n")
            append("\t").append(info.toString().replace("\n", "\n\t"))
            append("\n  \n}")
        }
    }
}