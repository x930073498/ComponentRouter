package com.x930073498.plugin.auto


import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.*

open class Auto {
    var enableRouter = true
    var enableDispatcher = true
    var enable = true
    var mavenUrl = "https://dl.bintray.com/x930073498/component"
    var version = "+"

    companion object {
        private const val GROUP = "com.x930073498.component"
        const val ARTIFACT_AUTO = "auto"
        const val ARTIFACT_CORE = "core"
        const val ARTIFACT_AUTO_STARTER_DISPATCHER = "auto-starter-dispatcher"
        const val ARTIFACT_ROUTER_ANNOTATIONS = "router-annotations"
        const val ARTIFACT_ROUTER_API = "router-api"
        const val ARTIFACT_ROUTER_COMPILER = "router-compiler"
        const val ARTIFACT_STARTER_DISPATCHER = "starter-dispatcher"

//        const val IMPLEMENTATION = "implementation"

        const val IMPLEMENTATION = "api"
        const val KAPT = "kapt"

    }

    private fun getDependency(artifact: String): String {
        return "$GROUP:$artifact:$version".apply {
            println("dependency=$this")
        }
    }

    internal data class Dependency(val command: String, val path: String) {
        fun dependency(scope: DependencyHandlerScope) {
            scope.run {
                add(command, path)
            }
        }
    }


    private fun Project.getDependency(plugin: Plugin<*>): List<Dependency> {
        val result = arrayListOf<Dependency>()
        result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_AUTO)))
        if (plugin is AppPlugin || plugin is LibraryPlugin) {
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_CORE)))
            if (enableRouter) {
                if (!plugins.hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
                    if (!plugins.hasPlugin("kotlin-android")) {
                        plugins.apply("kotlin-android")
                    }
                    plugins.apply(KOTLIN_KAPT_PLUGIN_ID)
                }
                result.add(
                    Dependency(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_API))
                )
                result.add(
                    Dependency(KAPT, getDependency(ARTIFACT_ROUTER_COMPILER))
                )
                result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_ANNOTATIONS)))
            }
            if (enableDispatcher) {
                result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_STARTER_DISPATCHER)))
                result.add(
                    Dependency(
                        IMPLEMENTATION,
                        getDependency(ARTIFACT_AUTO_STARTER_DISPATCHER)
                    )
                )
            }
        }

        return result
    }


    fun isValid() =  enable
    internal fun apply(project: Project) {
        if (!isValid()) {
            return
        }
        println("enter this line auto apply")
        println("enter this line repositoryUrl=$mavenUrl")
        project.predicate {
            doOn(AppPlugin::class, LibraryPlugin::class, JavaLibraryPlugin::class) {
                repositories {
                    maven(url = mavenUrl)
                }
                val list = getDependency(this)
                dependencies {
                    list.forEach {
                        it.dependency(this)
                    }
                }


            }
        }
    }

    override fun toString(): String {
        return "Auto(enableRouter=$enableRouter,enableDispatcher=$enableDispatcher,enable=$enable)"
    }
}