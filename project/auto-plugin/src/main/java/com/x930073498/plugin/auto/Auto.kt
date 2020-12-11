package com.x930073498.plugin.auto


import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.*

open class Auto {
    var enableRouter = true
    var enableDispatcher = true
    var enable = true

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
        fun getDependency(artifact: String): String {
            return "$GROUP:$artifact:+"
        }
    }


    fun isValid() = (enableRouter || enableDispatcher) && enable
    internal fun apply(project: Project) {
        if (!isValid()) {
            return
        }
        println("enter this line auto apply")
        project.predicate {
            doOn(AppPlugin::class, LibraryPlugin::class, JavaLibraryPlugin::class) {
                repositories {
//                    maven(url = "https://dl.bintray.com/x930073498/component")
                    maven(url = "file://E:\\demo\\Router\\project\\repository")
                }
                dependencies {
                    add(IMPLEMENTATION, getDependency(ARTIFACT_AUTO))
                }
                if (this !is AppPlugin && this !is LibraryPlugin) return@doOn
                dependencies {
                    add(IMPLEMENTATION, getDependency(ARTIFACT_CORE))
                }
                if (enableRouter) {
                    dependencies {
                        if (!plugins.hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
                            if (!plugins.hasPlugin("kotlin-android")) {
                                plugins.apply("kotlin-android")
                            }
                            plugins.apply(KOTLIN_KAPT_PLUGIN_ID)
                        }
                        add(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_API))
//                        add(KAPT, getDependency(ARTIFACT_ROUTER_COMPILER))
                        add(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_COMPILER))
                        add(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_ANNOTATIONS))
                    }
                }
                if (enableDispatcher) {
                    dependencies {
                        add(IMPLEMENTATION, getDependency(ARTIFACT_STARTER_DISPATCHER))
                        add(IMPLEMENTATION, getDependency(ARTIFACT_AUTO_STARTER_DISPATCHER))
                    }
                }
            }

        }
    }

    override fun toString(): String {
        return "Auto(enableRouter=$enableRouter,enableDispatcher=$enableDispatcher,enable=$enable)"
    }
}