import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
    jcenter()
}

plugins {
println("org.gradle.kotlin.dsl.embeddedKotlinVersion=$embeddedKotlinVersion")
    java
    `kotlin-dsl`
    kotlin("jvm")
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