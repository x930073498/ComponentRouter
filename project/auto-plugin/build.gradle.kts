import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="自动任务注入框架插件"
repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
    jcenter()
}

plugins {
    java
    `kotlin-dsl`
    kotlin("jvm")
}


sourceSets {
    var dir = project.rootDir.parentFile
    dir = File(dir, "buildPlugin")
    getByName("main").java.apply {
        setSrcDirs(mutableListOf(File(dir,"src/main/java")))
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.3")
    implementation("org.jacoco:org.jacoco.core:0.8.5")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}