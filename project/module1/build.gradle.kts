import com.x930073498.Versions
import com.x930073498.Libraries
import com.x930073498.PublishLibraries

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}
android {
    buildFeatures {
        viewBinding = true
    }
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode(Versions.versionCode)
        versionName(Versions.versionName)
        consumerProguardFile("consumer-rules.pro")
    }

    lintOptions {
        disable("GoogleAppIndexingWarning")
        baseline(file("lint-baseline.xml"))
    }
}
dependencies {
    implementation(Libraries.kotlin)
    implementation(Libraries.kotlinx_coroutines_android)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.navigation_fragment_ktx)
    kapt(project(":router-compiler"))
    implementation(project(":router-annotations"))
//    debugImplementation(project(":auto"))
//    debugImplementation(project(":core"))
//    debugImplementation(project(":router-api"))
//    releaseImplementation(PublishLibraries.auto)
//    releaseImplementation(PublishLibraries.core)
//    releaseImplementation(PublishLibraries.router_api)

    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":router-api"))
    implementation(project(":fragmentation"))
}