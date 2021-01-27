import com.x930073498.*
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="fastJson序列化实现"
plugins{
    kotlin("jvm")
}

dependencies{
    compileOnly(Libraries.kotlin)
    implementation(Libraries.fastJson)
    implementation(Libraries.kotlin_reflect)
    implementation(project(":auto"))
}