

buildscript {
    val repository=rootDir.absolutePath+File.separator+"repository"
    println("repository=$repository")
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven(url = "file://$repository")

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.4.21")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:+")
        classpath("com.x930073498.auto:auto-plugin:0.5")
    }

}
plugins {
    id("com.x930073498.build")
}
apply("plugin" to "com.x930073498.auto.plugin")
allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        google()
        jcenter()
    }

}