import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
    jcenter()
}
plugins {
    java
    `kotlin-dsl`
    kotlin("jvm") version "1.4.20"
}
gradlePlugin {
    plugins {
        create("defaultCompose") {
            id = "com.x930073498.build"
            implementationClass = "com.x930073498.plugin.BuildPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:4.1.1")
    implementation("org.jacoco:org.jacoco.core:0.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.20")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.novoda:bintray-release:0.9.2")
    implementation("com.github.panpf.bintray-publish:bintray-publish:1.0.0")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}