import com.x930073498.*

plugins {
    id("com.android.library")
    kotlin("android")
}

dependencies {
    compileOnly(Libraries.kotlin)
    implementation(Libraries.androidx_fragment_ktx)
    implementation(project(":core"))
    implementation(project(":router-annotations"))
    implementation(project(":auto"))
}