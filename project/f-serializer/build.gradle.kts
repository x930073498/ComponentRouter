import com.x930073498.*

plugins{
    kotlin("jvm")
}

dependencies{
    implementation(Libraries.kotlin)
    implementation(Libraries.fastJson)
    implementation(Libraries.kotlin_reflect)
    implementation(project(":auto"))
}