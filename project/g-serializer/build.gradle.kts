import com.x930073498.*

plugins{
    kotlin("jvm")
}

dependencies{
    implementation(Libraries.kotlin)
    implementation(Libraries.gson)
    implementation(project(":auto"))
}