package com.x930073498.component.auto.plugin


import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.*
import java.util.*

enum class SerializerType {
    K,//kotlin自带的序列化工具
    M,//moshi
    G,//gson
    F,//fastjson
    NONE //没有序列化
}

open class Auto {
    var enableDependency = true
    var enableRouter = true
    var enableDispatcher = true
    var enable = true
    var enableFragmentation = true
    private var serializer: String = SerializerType.NONE.name
    var mavenUrl = "https://dl.bintray.com/x930073498/component"
    var version = "+"
    private val serializerType: SerializerType
        get() {
            return runCatching { SerializerType.valueOf(serializer.toUpperCase(Locale.getDefault())) }.getOrNull()
                ?: SerializerType.NONE
        }

    companion object {
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
                result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_M_SERIALIZER)))
            }
            SerializerType.G -> {
                result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_G_SERIALIZER)))
                result.add(Dependency(IMPLEMENTATION, GSON_DEPENDENCY))
            }
            SerializerType.F -> {
                result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_F_SERIALIZER)))
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
                Dependency(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_API))
            )
            result.add(
                Dependency(KAPT, getDependency(ARTIFACT_ROUTER_COMPILER))
            )
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_ROUTER_ANNOTATIONS)))

        }

        fun addDispatcherDependency() {
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_STARTER_DISPATCHER)))
            result.add(
                Dependency(
                    IMPLEMENTATION,
                    getDependency(ARTIFACT_AUTO_STARTER_DISPATCHER)
                )
            )
        }

        fun addFragmentationDependency() {
            if (!enableRouter) {
                addRouterDependency()
            }
            result.add(Dependency(IMPLEMENTATION,getDependency(ARTIFACT_FRAGMENTATION)))
            result.add(Dependency(IMPLEMENTATION, KOTLIN_NAVIGATION_FRAGMENT_KTX_DEPENDENCY))
        }
        if (plugin is AppPlugin || plugin is LibraryPlugin) {
            result.add(Dependency(IMPLEMENTATION, getDependency(ARTIFACT_CORE)))
            if (enableRouter) {
                addRouterDependency()
            }
            if (enableDispatcher) {
                addDispatcherDependency()
            }
            if (enableFragmentation) {
                addFragmentationDependency()
            }
        }
        return result
    }


    fun isValid() = enable
    internal fun apply(project: Project) {
        if (!isValid() || !enableDependency) {
            return
        }
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