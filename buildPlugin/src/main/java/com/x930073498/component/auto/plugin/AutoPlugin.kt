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

    override fun apply(project: Project) {
        val auto = project.extensions.findByType<Auto>()
        if (auto == null)
            project.extensions.add("auto", Auto(project))
    }

}



