import com.x930073498.Versions

plugins {
    id("com.android.library")
    kotlin("android")

}
android {
    buildFeatures{
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
    implementation(project(":auto"))
    implementation(project(":starter-dispatcher"))
}
