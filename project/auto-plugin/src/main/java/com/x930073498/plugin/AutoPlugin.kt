package com.x930073498.plugin

import com.android.build.gradle.AppPlugin
import com.x930073498.plugin.register.AutoRegisterConfig
import com.x930073498.plugin.register.RegisterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoPlugin : Plugin<Project> {
    private fun init(project: Project, transform: RegisterTransform) {
        val config = AutoRegisterConfig()
        config.project = project
        config.convertConfig()
        transform.config = config
    }

    private fun registerPlugin(project: Project) {
        with(project) {
            println("enter this line project=$this")
            val transform = RegisterTransform(this)
            init(this, transform)
            android.registerTransform(transform)
        }
    }

    private fun initPlugin(project: Project) {
        with(project) {
            if (plugins.hasPlugin(AppPlugin::class.java)) {
                registerPlugin(this)
            } else {
                plugins.whenPluginAdded {
                    if (this is AppPlugin) {
                        registerPlugin(this@with)
                    }
                }
            }
        }
    }

    override fun apply(project: Project) {
        println("enter this line auto plugin")
        if (project.subprojects.size == 0) {
           initPlugin(project)
        } else
            project.subprojects {
              initPlugin(this)
            }
    }
}