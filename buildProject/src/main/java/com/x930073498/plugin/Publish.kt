package com.x930073498.plugin

import com.android.build.gradle.BaseExtension
import com.x930073498.plugin.Publish.GROUP
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
val Project.android: BaseExtension
    get() = project.extensions.getByName("android") as BaseExtension
object Publish {
    const val GROUP = "com.x930073498.component"
    const val VERSION = "0.0.16"

    data class PublishInfo(
        val group: String,
        val artifact: String,
        val version: String,
        val desc: String
    )
}

private fun Project.toPublishInfo() = Publish.PublishInfo(
    group.toString(),
    name,
    version.toString(),
    description ?: ""
).apply { println(this) }

val Project.publishInfo: Publish.PublishInfo?
    get() {
        return if (group == GROUP) toPublishInfo()
        else null
    }

