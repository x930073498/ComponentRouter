import com.x930073498.Versions
import com.x930073498.Libraries

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
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_appcompat)
    kapt(project(":router-compiler"))
    implementation(project(":router-annotations"))
    implementation(project(":auto"))
    implementation(project(":router-api"))
}