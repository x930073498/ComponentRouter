package com.x930073498.component.auto.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.*
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.execution.TaskActionListener
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.TaskState
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.findPlugin
import kotlin.reflect.KClass

class AutoPlugin : Plugin<Project> {
    private fun Project.registerTransform() {
        val result = ASMTransform(this)
        android.registerTransform(result)

    }

    override fun apply(project: Project) {
        project.extensions.add("auto", Auto(project))
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