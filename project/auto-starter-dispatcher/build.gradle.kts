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
    buildTypes {
        getByName("release") {
            minifyEnabled(false)
        }
    }

    lintOptions {
        disable("GoogleAppIndexingWarning")
        baseline(file("lint-baseline.xml"))
    }

}
dependencies {
//    debugImplementation(project(":auto"))
//    debugImplementation(project(":core"))
//    debugImplementation(project(":starter-dispatcher"))
//    releaseImplementation(PublishLibraries.auto)
//    releaseImplementation(PublishLibraries.core)
//    releaseImplementation(PublishLibraries.starter_dispatcher)

    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":starter-dispatcher"))
//    compileOnly(project(":auto"))
//    compileOnly(project(":core"))
//    compileOnly(project(":starter-dispatcher"))
}
