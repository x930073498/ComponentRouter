package com.x930073498.component.auto.plugin


import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.*
import org.gradle.util.ConfigureUtil
import java.util.*

enum class SerializerType {
    K,//kotlin自带的序列化工具
    M,//moshi
    G,//gson
    F,//fastjson
    NONE //没有序列化
}

class AutoOptions internal constructor(
    var enableDependency: Boolean = true,
    var enableRouter: Boolean = true,
    var enableDispatcher: Boolean = true,
    var enable: Boolean = true,
    var enableFragmentation: Boolean = true,
    var serializer: String = SerializerType.NONE.name,
    var mavenUrl: String = "",
    var versionPattern: String = "+",
    var enableLog: Boolean = false
) {
    internal val map: MutableMap<String, AutoOptions> = mutableMapOf()
    fun child(name: String, options: AutoOptions.() -> Unit) {
        val option = AutoOptions(
            enableDependency,
            enableRouter,
            enableDispatcher,
            enable,
            enableFragmentation,
            serializer,
            mavenUrl,
            versionPattern,
            enableLog
        )
        options(option)
        map[name] = option
    }

    fun child(name: String, action: Closure<AutoOptions>) {
        val option = AutoOptions(
            enableDependency,
            enableRouter,
            enableDispatcher,
            enable,
            enableFragmentation,
            serializer,
            mavenUrl,
            versionPattern,
            enableLog
        )
        ConfigureUtil.configure(action, option)
        map[name] = option
    }
}

open class Auto constructor(val project: Project) {
    private fun serializerType(options: AutoOptions): SerializerType {
        return runCatching { SerializerType.valueOf(options.serializer.toUpperCase(Locale.getDefault())) }.getOrNull()
            ?: SerializerType.NONE
    }

    fun options(auto: AutoOptions. () -> Unit) {
        val options = AutoOptions()
        auto(options)
        this.apply(project, options)
    }

    fun options(action: Closure<AutoOptions>) {
        val options = AutoOptions()
        ConfigureUtil.configure(action, options)
        this.apply(project, options)
    }


    private companion object {
        private const val GROUP = "com.x930073498.component"
        const val ARTIFACT_AUTO = "auto"
        const val ARTIFACT_CORE = "core"
        const val ARTIFACT_AUTO_STARTER_DISPATCHER = "auto-starter-dispatcher"
        const val ARTIFACT_ROUTER_ANNOTATIONS = "router-annotations"
        const val ARTIFACT_ROUTER_API = "router-api"
        const val ARTIFACT_FRAGMENTATION = "fragmentation"
        const val ARTIFACT_ROUTER_COMPILER = "router-compiler"
        const val ARTIFACT_K_SERIALIZER = "k-serializer"
        const val ARTIFACT_M_SERIALIZER = "m-serializer"
        const val ARTIFACT_G_SERIALIZER = "g-serializer"
        const val ARTIFACT_F_SERIALIZER = "f-serializer"
        const val ARTIFACT_STARTER_DISPATCHER = "starter-dispatcher"
        const val MOSHI_DEPENDENCY = "com.squareup.moshi:moshi:1.11.0"
        const val MOSHI_CODEGEN_DEPENDENCY = "com.squareup.moshi:moshi-kotlin-codegen:1.11.0"
        const val GSON_DEPENDENCY = "com.google.code.gson:gson:2.8.6"
        const val FAST_JSON_DEPENDENCY = "com.alibaba:fastjson:1.1.72.android"
        const val KOTLIN_REFLECT_DEPENDENCY = "org.jetbrains.kotlin:kotlin-reflect:1.4.21"
        const val KOTLIN_NAVIGATION_FRAGMENT_KTX_DEPENDENCY =
            "androidx.navigation:navigation-fragment-ktx:2.3.2"
//        const val IMPLEMENTATION = "implementation"

        const val IMPLEMENTATION = "api"
        const val KAPT = "kapt"
        const val REMOTE_MAVEN_URL="https://dl.bintray.com/x930073498/component"
//        const val KAPT = "annotationProcessor"

    }

    private fun getDependency(artifact: String, options: AutoOptions): String {
        return "$GROUP:$artifact:${options.versionPattern}".apply {
            if (options.enableLog)
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


    private fun Project.getDependency(plugin: Plugin<*>, options: AutoOptions): List<Dependency> {
        val result = arrayListOf<Dependency>()
        result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_AUTO, options)))
        val serializerType = serializerType(options)
        if (options.enableLog)
            println("serializerType=$serializerType")
        when (serializerType) {
            SerializerType.K -> {
//                if (KotlinVersion.CURRENT.also {
//                        println("kotlin version=$it")
//                    }.run {
//                        major >= 1 && minor >= 4
//                    }) {
//                    plugins.apply {
//                        apply("org.jetbrains.kotlin.plugin.serialization")
//                        this.withId("org.jetbrains.kotlin.plugin.serialization") {
//                            version = "1.4.10"
//                        }
//                    }
//                    result.add(
//                        Dependency(
//                            IMPLEMENTATION,
//                            getDependency(ARTIFACT_K_SERIALIZER)
//                        )
//                    )
//                }
            }
            SerializerType.M -> {
                if (!plugins.hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
                    if (plugin !is JavaLibraryPlugin) {
                        plugins.apply("kotlin-android")
                    } else {
                        plugins.apply("kotlin")
                    }
                    plugins.apply(KOTLIN_KAPT_PLUGIN_ID)
                }
                result.add(Dependency(KAPT, MOSHI_CODEGEN_DEPENDENCY))
                result.add(Dependency(IMPLEMENTATION, MOSHI_DEPENDENCY))
                result.add(
                    Dependency(
                        IMPLEMENTATION,
                        getDependency(ARTIFACT_M_SERIALIZER, options)
                    )
                )
            }
            SerializerType.G -> {
                result.add(
                    Dependency(
                        IMPLEMENTATION,
                        getDependency(ARTIFACT_G_SERIALIZER, options)
                    )
                )
                result.add(Dependency(IMPLEMENTATION, GSON_DEPENDENCY))
            }
            SerializerType.F -> {
                result.add(
                    Dependency(
                        IMPLEMENTATION,
                        getDependency(ARTIFACT_F_SERIALIZER, options)
                    )
                )
                result.add(Dependency(IMPLEMENTATION, FAST_JSON_DEPENDENCY))
                result.add(Dependency(IMPLEMENTATION, KOTLIN_REFLECT_DEPENDENCY))
            }
            SerializerType.NONE -> {
//do nothing
            }
        }

        fun addRouterDependency() {
            if (!plugins.hasPlugin(KOTLIN_KAPT_PLUGIN_ID)) {
                if (!plugins.hasPlugin("kotlin-android")) {
                    plugins.apply("kotlin-android")
                }
                plugins.apply(KOTLIN_KAPT_PLUGIN_ID)
            }
            result.add(
                Dependency(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_API, options))
            )
            result.add(
                Dependency(KAPT, getDependency(ARTIFACT_ROUTER_COMPILER, options))
            )
            result.add(
                Dependency(
                    IMPLEMENTATION,
                    getDependency(ARTIFACT_ROUTER_ANNOTATIONS, options)
                )
            )

        }

        fun addDispatcherDependency() {
            result.add(
                Dependency(
                    IMPLEMENTATION,
                    getDependency(ARTIFACT_STARTER_DISPATCHER, options)
                )
            )
            result.add(
                Dependency(
                    IMPLEMENTATION,
                    getDependency(ARTIFACT_AUTO_STARTER_DISPATCHER, options)
                )
            )
        }

        fun addFragmentationDependency() {
            if (!options.enableRouter) {
                addRouterDependency()
            }
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_FRAGMENTATION, options)))
            result.add(Dependency(IMPLEMENTATION, KOTLIN_NAVIGATION_FRAGMENT_KTX_DEPENDENCY))
        }
        if (plugin is AppPlugin || plugin is LibraryPlugin) {
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_CORE, options)))
            if (options.enableRouter) {
                addRouterDependency()
            }
            if (options.enableDispatcher) {
                addDispatcherDependency()
            }
            if (options.enableFragmentation) {
                addFragmentationDependency()
            }
        }
        return result
    }


    private fun isValid(options: AutoOptions) = options.enable

    private fun Project.setDependency(plugin: Plugin<*>, options: AutoOptions) {
        if (options.enableDependency) {
            repositories {
                if (options.mavenUrl.isEmpty()) {
                    maven(url=REMOTE_MAVEN_URL)
                } else
                    maven(url = options.mavenUrl)
            }
            val list = getDependency(plugin, options)
            dependencies {
                list.forEach {
                    it.dependency(this)
                }
            }
        }
    }

    private fun apply(project: Project, options: AutoOptions) {
        if (!isValid(options)) {
            return
        }
        project.allprojects {
            if (this == rootProject) return@allprojects
            val childOptions = options.map[name] ?: options
            if (!childOptions.enable) return@allprojects
            plugins.whenPluginAdded {
                if (this is AppPlugin) {
                    android.registerTransform(ASMTransform(this@allprojects))
                    setDependency(this, childOptions)
                } else if (this is LibraryPlugin || this is JavaLibraryPlugin) {
                    setDependency(this, childOptions)
                }
            }
        }

    }


}