import com.x930073498.Versions

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}
android {
    buildFeatures{
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha04")
    implementation("androidx.startup:startup-runtime:1.0.0")
    implementation("androidx.paging:paging-runtime-ktx:3.0.0-alpha09")
    implementation(project(":router-annotations"))
    implementation(project(":auto"))
    implementation(project(":router-api"))
    implementation(project(":module1"))
    kapt(project(":router-compiler"))
}
