package com.x930073498.component.auto.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.x930073498.component.auto.plugin.register.AutoRegisterConfig
import com.x930073498.component.auto.plugin.register.RegisterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.findPlugin
import kotlin.reflect.KClass

class AutoPlugin : Plugin<Project> {
    private fun init(project: Project, transform: RegisterTransform) {
        val config = AutoRegisterConfig()
        config.project = project
        config.convertConfig()
        transform.config = config
    }


    private fun Project.doOnIsApp(action: AppPlugin.() -> Unit) {
        doOn(AppPlugin::class) {
            this as AppPlugin
            action()
        }
    }

    private fun Project.doOnIsLibrary(action: LibraryPlugin.() -> Unit) {
        doOn(LibraryPlugin::class) {
            this as LibraryPlugin
            action()
        }
    }


    private fun Project.registerTransform() {
//        val transform = RegisterTransform(this)
//        init(this, transform)
//        android.registerTransform(transform)
        android.registerTransform(ASMTransform(this))
    }

    override fun apply(project: Project) {
        fun findAuto() = project.extensions.findByType<Auto>()
        project.extensions.add("auto", Auto())
        fun isRootProject(other: Project): Boolean {
            return project.path == other.path
        }
        project.predicate {
            doOn(AppPlugin::class, LibraryPlugin::class, JavaLibraryPlugin::class) {
                if (isRootProject(this@predicate)) {
                    afterEvaluate {
                        val auto = findAuto() ?: return@afterEvaluate
                        if (auto.isValid()) {
                            registerTransform()
                            auto.apply(this@predicate)
                        }
                    }
                } else {
                    val auto = findAuto() ?: return@doOn
                    if (!auto.isValid()) return@doOn
                    auto.apply(this@predicate)
                    if (this is AppPlugin) {
                        registerTransform()
                    }
                }
            }
        }

    }
}

fun Project.predicate(action: Project.() -> Unit) {
    action(this)
    subprojects {
        action(this)
    }
}

fun Project.doOn(
    vararg pluginClass: KClass<out Plugin<*>>,
    action: Plugin<*>.() -> Unit
) {
    val plugin = pluginClass.mapNotNull { plugins.findPlugin(it) }.firstOrNull()
    if (plugin != null) {
        action(plugin)
        return
    }
    plugins.whenPluginAdded {
        if (pluginClass.any { it.isInstance(this) }) {
            action(this)
            return@whenPluginAdded
        }
    }
}