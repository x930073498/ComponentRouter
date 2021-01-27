import com.x930073498.Libraries
group=com.x930073498.plugin.Publish.GROUP
version=com.x930073498.plugin.Publish.VERSION
description="路由注解processor"
plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(Libraries.kotlin)
    kapt(Libraries.google_auto_service)
    compileOnly(Libraries.google_auto_service)
    implementation(Libraries.kotlin_poet)
    implementation(Libraries.kotlin_reflect)
    implementation(Libraries.androidx_annotation)
    implementation(Libraries.kotlin_compiler_embeddable)
    implementation(project(":router-annotations"))
}