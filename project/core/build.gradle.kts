import com.x930073498.Libraries
import com.x930073498.Versions

plugins {
    id("com.android.library")
    kotlin("android")
}

dependencies {
    compileOnly(Libraries.kotlin)
    implementation(Libraries.startup)
//    debugImplementation(project(":auto"))
//    releaseImplementation(PublishLibraries.auto)
    implementation(Libraries.androidx_appcompat)
    implementation(project(":auto"))

}