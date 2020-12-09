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
    implementation(project(":auto"))
    implementation(project(":starter-dispatcher"))
}
