import com.x930073498.*

plugins {
//    id("com.x930073498.auto.plugin")
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")

}
android {
    buildFeatures {
        viewBinding = true
    }
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        applicationId("com.x930073498.kotlinpoet")
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
    implementation(project(":router-annotations"))
    implementation(project(":auto"))
    implementation(project(":auto-starter-dispatcher"))
    implementation(project(":starter-dispatcher"))
    implementation(project(":router-api"))
    implementation(project(":module1"))
    kapt(project(":router-compiler"))
}
