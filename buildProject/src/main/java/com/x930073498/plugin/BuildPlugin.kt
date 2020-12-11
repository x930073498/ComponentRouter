package com.x930073498.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.kotlin.konan.file.File
import com.github.panpf.bintray.publish.PublishExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*


fun Project.isRootProject(): Boolean {
    return this == rootProject
}


class BuildPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            BinaryInfo.bintrayKey = getProperty("bintrayKey")
            BinaryInfo.bintrayUser = getProperty("bintrayUser")
            BinaryInfo.repoName = getProperty("repoName")
            BinaryInfo.userOrg = getProperty("userOrg")
            println("BinaryInfo=${BinaryInfo}")
            if (isRootProject()) {
                subprojects {
                    initPublish(this)
                }
            } else {
                initPublish(this)
            }
        }
    }

    private fun Project.getProperty(key: String): String {
        return rootProject.extra.run {
            runCatching {
                println("hasProperty=${hasProperty(key)}")
                println("has=${has(key)}")
                this[key].toString()
            }.getOrElse { "" }
        }
    }

    private fun initPublish(project: Project) {
        with(project) {
            if (BinaryInfo.valid()) {
                publishInfo?.let {
                    tasks.withType<Javadoc> {
                        options.encoding = "UTF-8"
                    }
//                    val path = "${rootProject.rootDir}${File.separator}upload.gradle"
//                    this.apply(path)
                    plugins.apply("com.github.panpf.bintray-publish")
                    plugins.apply(MavenPublishPlugin::class.java)
                    configure<PublishExtension> {
                        userOrg = BinaryInfo.userOrg
                        groupId = it.group
                        repoName = BinaryInfo.repoName
                        artifactId = it.artifact
                        publishVersion = it.version
                        bintrayUser = BinaryInfo.bintrayUser
                        desc = it.desc
                        bintrayKey = BinaryInfo.bintrayKey
                        setLicences("Apache-2.0")
                        dryRun = false
                        website = "https://github.com/x930073498/component"
                        println("project=${name},groupId=$groupId")
                    }

                    afterEvaluate {
                        configure<PublishingExtension> {
                            this.repositories {
                                maven(uri("../repository"))
                            }
                            this.publications {
                                create<MavenPublication>("full") {
                                    groupId = it.group
                                    artifactId = it.artifact
                                    version = it.version
                                    println(
                                        "components=${
                                            components.fold(StringBuffer()) { buffer, it ->
                                                buffer.append(it.name).append("")
                                            }
                                        }")
                                    if (components.isNotEmpty()) {
                                        val component =
                                            components.firstOrNull { it.name == "kotlin" || it.name == "release" }
                                                ?: components.first()
                                        println("componentName=${component.name}")
                                        from(component)
                                    }

                                }
                            }

                        }
                    }
                }
            }
        }
    }
}