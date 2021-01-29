import com.x930073498.*

plugins {
    id("com.android.library")
    kotlin("android")
}

dependencies {
    implementation(Libraries.kotlin)
    implementation(Libraries.androidx_fragment_ktx)
    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":router-api"))
    implementation(Libraries.navigation_fragment_ktx)
}