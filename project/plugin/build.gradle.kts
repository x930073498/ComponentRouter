import com.x930073498.plugin.android
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
    kotlin("jvm")
    `maven-publish`
}

group = "com.x930073498.auto"
version = "0.5"





afterEvaluate {

}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.3")
    implementation("org.jacoco:org.jacoco.core:0.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
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