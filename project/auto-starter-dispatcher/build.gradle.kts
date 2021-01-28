import com.x930073498.*

plugins {
    id("com.android.library")
    kotlin("android")
}


dependencies {
    implementation(project(":auto"))
    implementation(project(":core"))
    implementation(project(":starter-dispatcher"))

}
