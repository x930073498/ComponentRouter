package com.x930073498.component.auto.plugin


import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.tasks.KOTLIN_KAPT_PLUGIN_ID
import com.x930073498.component.auto.plugin.options.RouterOptions
import groovy.lang.Closure
import org.gradle.api.Action
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


open class Auto constructor(val project: Project) {

    private var options = AutoOptions()
    private var actionHasDone = false


    fun options(action: Closure<AutoOptions>) {
        if (actionHasDone) return
        options = ConfigureUtil.configure(action, options)
        options.apply(project)
        actionHasDone = true
    }

    fun options(action: Action<AutoOptions>) {
        if (actionHasDone) return
        action.execute(options)
        options.apply(project)
        actionHasDone = true
    }


    companion object {
        const val GROUP = "com.x930073498.component"
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
        const val REMOTE_MAVEN_URL = "https://dl.bintray.com/x930073498/component"
//        const val KAPT = "annotationProcessor"

    }


    internal data class Dependency(val command: String, val path: String) {
        fun dependency(scope: DependencyHandlerScope) {
            scope.run {
                add(command, path)
            }
        }
    }


}

fun Project.setDependency(plugin: Plugin<*>, options: AutoOptions) {
    if (options.enableDependency) {
        repositories {
            if (options.mavenUrl.isEmpty()) {
                maven(url = Auto.REMOTE_MAVEN_URL)
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

private fun Project.getDependency(plugin: Plugin<*>, options: AutoOptions): List<Auto.Dependency> {
    val result = arrayListOf<Auto.Dependency>()
    result.add(Auto.Dependency(Auto.IMPLEMENTATION, getDependency(Auto.ARTIFACT_AUTO, options)))
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
            result.add(Auto.Dependency(Auto.KAPT, Auto.MOSHI_CODEGEN_DEPENDENCY))
            result.add(Auto.Dependency(Auto.IMPLEMENTATION, Auto.MOSHI_DEPENDENCY))
            result.add(
                Auto.Dependency(
                    Auto.IMPLEMENTATION,
                    getDependency(Auto.ARTIFACT_M_SERIALIZER, options)
                )
            )
        }
        SerializerType.G -> {
            result.add(
                Auto.Dependency(
                    Auto.IMPLEMENTATION,
                    getDependency(Auto.ARTIFACT_G_SERIALIZER, options)
                )
            )
            result.add(Auto.Dependency(Auto.IMPLEMENTATION, Auto.GSON_DEPENDENCY))
        }
        SerializerType.F -> {
            result.add(
                Auto.Dependency(
                    Auto.IMPLEMENTATION,
                    getDependency(Auto.ARTIFACT_F_SERIALIZER, options)
                )
            )
            result.add(Auto.Dependency(Auto.IMPLEMENTATION, Auto.FAST_JSON_DEPENDENCY))
            result.add(Auto.Dependency(Auto.IMPLEMENTATION, Auto.KOTLIN_REFLECT_DEPENDENCY))
        }
        SerializerType.NONE -> {
//do nothing
        }
    }


    fun addDispatcherDependency() {
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                getDependency(Auto.ARTIFACT_STARTER_DISPATCHER, options)
            )
        )
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                getDependency(Auto.ARTIFACT_AUTO_STARTER_DISPATCHER, options)
            )
        )
    }

    fun addFragmentationDependency() {
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                getDependency(Auto.ARTIFACT_FRAGMENTATION, options)
            )
        )
        result.add(
            Auto.Dependency(
                Auto.IMPLEMENTATION,
                Auto.KOTLIN_NAVIGATION_FRAGMENT_KTX_DEPENDENCY
            )
        )
    }
    if (plugin is AppPlugin || plugin is LibraryPlugin) {
        result.add(Auto.Dependency(Auto.IMPLEMENTATION, getDependency(Auto.ARTIFACT_CORE, options)))
        if (options.enableDispatcher) {
            addDispatcherDependency()
        }
        if (options.enableFragmentation) {
            addFragmentationDependency()
        }
    }
    return result
}

fun getDependency(artifact: String, options: AutoOptions): String {
    return "${Auto.GROUP}:$artifact:${options.versionPattern}".apply {
        if (options.enableLog)
            println("dependency=$this")
    }
}

fun getDependency(artifact: String, version: String,options: AutoOptions): String {
    return "${Auto.GROUP}:$artifact:$version".apply {
        if (options.enableLog)
            println("dependency=$this")
    }
}

private fun serializerType(options: AutoOptions): SerializerType {
    return runCatching { SerializerType.valueOf(options.serializer.toUpperCase(Locale.getDefault())) }.getOrNull()
        ?: SerializerType.NONE
}