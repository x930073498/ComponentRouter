import com.x930073498.*

plugins{
    kotlin("jvm")
}

dependencies{
    implementation(Libraries.kotlin)
    implementation(Libraries.moshi)
    implementation(project(":auto"))
}