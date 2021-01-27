buildscript {
    val properties = mapOf<String, String>().toProperties()
    val file = file("local.properties")
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
        classpath("com.x930073498.component:auto-plugin:0.0.15")
    }

}


plugins.apply("com.x930073498.component.auto.plugin")
configure<com.x930073498.component.auto.plugin.Auto> {
    options {
     enableDependency = false
    }
}
plugins {
    id("com.x930073498.build")
//    id("com.x930073498.component.auto.plugin")
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

