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

abstract class Options constructor() {
    internal abstract fun apply(
        project: Project,
        plugin: Plugin<*>,
        options: AutoOptions
    )

}


