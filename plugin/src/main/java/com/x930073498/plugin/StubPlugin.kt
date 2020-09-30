package com.x930073498.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class StubPlugin:Plugin<Project> {
    override fun apply(project: Project) {
        println("测试----")
    }
}