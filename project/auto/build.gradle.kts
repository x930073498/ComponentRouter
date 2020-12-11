//plugins {
////    java
////    kotlin("jvm")
//    id("com.android.library")
//    kotlin("android")
//}
//android {
//    compileSdkVersion(com.x930073498.Versions.compileSdk)
//    defaultConfig {
//        minSdkVersion(com.x930073498.Versions.minSdk)
//        targetSdkVersion(com.x930073498.Versions.targetSdk)
//        versionCode(com.x930073498.Versions.versionCode)
//        versionName(com.x930073498.Versions.versionName)
//        consumerProguardFile("consumer-rules.pro")
//    }
//
//    lintOptions {
//        disable("GoogleAppIndexingWarning")
//        baseline(file("lint-baseline.xml"))
//    }
//}
import com.x930073498.*
plugins{
    java
    kotlin("jvm")
}

dependencies{
    implementation(Libraries.kotlin)
}