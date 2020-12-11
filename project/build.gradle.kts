import com.x930073498.plugin.BinaryInfo
import java.util.Properties

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
        maven(url = "file://$repository")

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.4.21")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:+")
    }

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

