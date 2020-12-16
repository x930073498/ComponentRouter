import com.x930073498.*

plugins {
//    id("com.x930073498.auto.plugin")
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.4.20"
//    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")

}
println("group=$group")

android {
    buildFeatures {
        viewBinding = true
    }
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
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
    implementation(Libraries.hilt_android)
    implementation(Libraries.hilt_lifecycle_viewmodel)
    kapt(Libraries.hilt_android_compiler)
    kapt(Libraries.hilt_compiler)

    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.constraintlayout)
    implementation(Libraries.kotlinx_coroutines_core)
    implementation(Libraries.datastore_preferences)
    implementation(Libraries.startup)
    implementation(Libraries.agent_web)
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
//    implementation(Libraries.gson)
//    implementation(project(":g-serializer"))
//fastjson
    implementation(Libraries.fastJson)
    implementation(project(":f-serializer"))
    kapt(project(":router-compiler"))
}
