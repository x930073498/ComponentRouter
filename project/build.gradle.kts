import com.x930073498.PublishLibraries.auto
import com.x930073498.plugin.BinaryInfo
import java.util.Properties
import com.x930073498.component.auto.plugin.Auto
buildscript {
    val properties = mapOf<String, String>().toProperties()
    val file = file("local.properties")
    println("enter this line local.properties isExit=${file.exists()}")
    if (file.exists()) {
        properties.load(file.inputStream())
        extra.apply {
            this["repoName"] = properties["repoName"] ?: ""
            this["bintrayKey"] = properties["bintrayKey"] ?: ""
            this["bintrayUser"] = properties["bintrayUser"] ?: ""
            this["userOrg"] = properties["userOrg"] ?: ""
        }

    }
    val repository = rootDir.absolutePath + File.separator + "repository"
    println("repository=$repository")
    repositories {
        mavenCentral()
        google()
        jcenter()
//        maven("https://dl.bintray.com/x930073498/component")
        maven(url = "file://$repository")

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.4.21")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:+")
        classpath("com.x930073498.component:auto-plugin:0.0.9")
    }

}


plugins.apply("com.x930073498.component.auto.plugin")

configure<Auto> {
    val repository = rootDir.absolutePath + File.separator + "repository"
    this.enableDependency=false
    this.mavenUrl=repository

}
plugins {
    id("com.x930073498.build")
}

allprojects {
    repositories {
        mavenCentral()
        val repository = rootDir.absolutePath + File.separator + "repository"
        maven(url = "file://$repository")
        maven("https://jitpack.io")
        google()
        jcenter()
    }

}

