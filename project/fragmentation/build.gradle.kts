import com.x930073498.*
group = com.x930073498.plugin.Publish.GROUP
version = com.x930073498.plugin.Publish.VERSION
description = "基于router的navigation实现"
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
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
    compileOnly(Libraries.kotlin)
    implementation(Libraries.androidx_fragment_ktx)
    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":router-api"))
    implementation(Libraries.navigation_fragment_ktx)
}