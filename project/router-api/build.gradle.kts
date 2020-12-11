import com.x930073498.*

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
//    debugImplementation(project(":core"))
//    debugImplementation(project(":auto"))
//    releaseImplementation(PublishLibraries.core)
//    releaseImplementation(PublishLibraries.auto)
    implementation(project(":core"))
    implementation(project(":auto"))
}