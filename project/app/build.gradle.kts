import com.x930073498.*

plugins {
//    id("com.x930073498.auto.plugin")
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.4.20"
    id("kotlin-parcelize")
    id("kotlin-android")

}
println("group=$group")

android {
    buildFeatures {
        viewBinding = true
    }
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        signingConfig = signingConfigs.create("release") {
            storeFile(rootProject.file("../key-store.jks").apply {
                println(this)
            })
            keyAlias("app")
            storePassword("123456")
            keyPassword("123456")
        }
        multiDexEnabled = true
        applicationId("com.x930073498.component")
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode(Versions.versionCode)
        versionName(Versions.versionName)
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(true)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lintOptions {
        disable("GoogleAppIndexingWarning")
        baseline(file("lint-baseline.xml"))
    }

}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.multidex)
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")


    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.constraintlayout)
    implementation(Libraries.kotlinx_coroutines_core)
    implementation(Libraries.datastore_preferences)
    implementation(Libraries.startup)
    implementation(Libraries.lifecycle_viewmodel_ktx)
    implementation(Libraries.lifecycle_livedata_ktx)
    implementation(Libraries.lifecycle_runtime_ktx)
    implementation(Libraries.lifecycle_viewmodel_savedstate)
    implementation(Libraries.lifecycle_common_java8)
    implementation(Libraries.lifecycle_service)
    implementation(Libraries.lifecycle_process)
    implementation(Libraries.agent_web)
    implementation(Libraries.mvrx)
    implementation(project(":router-annotations"))
    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":auto-starter-dispatcher"))
    implementation(project(":starter-dispatcher"))
    implementation(project(":router-api"))
    implementation(project(":module1"))
    //kotlin-serializer
    //    implementation(Libraries.kotlinx_serialization_json)
//    implementation(project(":k-serializer"))
    //moshi
//    implementation(project(":m-serializer"))
//    kapt(Libraries.moshi_codegen)
//    implementation(Libraries.moshi)
    //gson
    implementation(Libraries.gson)
    implementation(project(":g-serializer"))
//fastjson
//    implementation(Libraries.fastJson)
//    implementation(project(":f-serializer"))
    kapt(project(":router-compiler"))

    //navigation
    implementation(Libraries.navigation_fragment_ktx)
    implementation(project(":fragmentation"))
    implementation(Libraries.navigation_ui_ktx)

}
