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

    override fun apply(project: Project) {
        project.subprojects {
            plugins.whenPluginAdded {
                if (this is AppPlugin) {
                    val transform = RegisterTransform(this@subprojects)
                    init(this@subprojects, transform)
                    android.registerTransform(transform)
                }
            }
        }
    }
}