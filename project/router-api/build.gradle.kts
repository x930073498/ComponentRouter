import com.x930073498.*
plugins{
    id("com.android.library")
    kotlin("android")
}
android{
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
dependencies{
    implementation(Libraries.kotlin)
    implementation(Libraries.androidx_fragment_ktx)
    implementation(project(":auto"))
}