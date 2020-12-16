import com.x930073498.*
plugins{
    kotlin("jvm")
}

dependencies{
    compileOnly(Libraries.kotlin)
    implementation(Libraries.fastJson)
    implementation(project(":auto"))
}