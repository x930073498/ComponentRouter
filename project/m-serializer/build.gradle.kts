import com.x930073498.*
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="moshi序列化实现"
plugins{
    kotlin("jvm")
}

dependencies{
    compileOnly(Libraries.kotlin)
    implementation(Libraries.moshi)
    implementation(project(":auto"))
}