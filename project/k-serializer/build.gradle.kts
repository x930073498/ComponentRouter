import com.x930073498.*
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="kotlin自带序列化实现"
plugins{
    kotlin("jvm")
}

dependencies{
    compileOnly(Libraries.kotlin)
    implementation(Libraries.kotlinx_serialization_json)
    implementation(project(":auto"))
}