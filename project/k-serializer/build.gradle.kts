import com.x930073498.*

plugins{
    kotlin("jvm")
}

dependencies{
    implementation(Libraries.kotlin)
    implementation(Libraries.kotlinx_serialization_json)
    implementation(project(":auto"))
}