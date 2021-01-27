import com.x930073498.*
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="路由注解库"
plugins{
    kotlin("jvm")
}

dependencies{
    compileOnly(Libraries.kotlin)
    compileOnly(Libraries.androidx_annotation)
}