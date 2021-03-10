package com.x930073498.component.auto.plugin.options

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import com.x930073498.component.auto.plugin.Auto
import com.x930073498.component.auto.plugin.AutoOptions
import com.x930073498.component.auto.plugin.android
import com.x930073498.component.auto.plugin.getDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RouterOptions : Options() {
    private var isDocEnable = false

    private var enable = true

    private var versionPattern = ""
    fun enableDoc() {
        isDocEnable = true
    }

    fun disable() {
        enable = false
    }

    fun routerVersion(version: String) {
        this.versionPattern = version
    }

    private fun Project.addRouterDependency(options: AutoOptions) {
        if (!options.enableDependency) return
        val result = arrayListOf<Auto.Dependency>()
        val version = if (versionPattern.isEmpty()) options.versionPattern else versionPattern
        if (!plugins.hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
            if (!plugins.hasPlugin("kotlin-android")) {
                plugins.apply("kotlin-android")
            }
            plugins.apply(KOTLIN_KAPT_PLUGIN_ID)
        }
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                getDependency(Auto.ARTIFACT_ROUTER_API, version, options)
            )
        )
        result.add(
            Auto.Dependency(
                Auto.KAPT,
                getDependency(Auto.ARTIFACT_ROUTER_COMPILER, version, options)
            )
        )
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                getDependency(Auto.ARTIFACT_ROUTER_ANNOTATIONS, version, options)
            )
        )
        dependencies {
            result.forEach {
                it.dependency(this)
            }
        }


    }

    override fun apply(project: Project, plugin: Plugin<*>, options: AutoOptions) {
        if (!options.enable || !enable) return
        when (plugin) {
            is AppPlugin, is LibraryPlugin -> {
                project.addRouterDependency(options)
                if (isDocEnable && options.enableDependency) {
                    project.android.defaultConfig {
                        javaCompileOptions {
                            annotationProcessorOptions {
                                argument("router_document_enable", "true")
                                argument("router_document_project_name", project.name)
                            }
                        }
                    }
                }
            }
        }


    }


}