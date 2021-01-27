package com.x930073498.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import com.github.panpf.bintray.publish.PublishExtension
import com.x930073498.Versions
import org.gradle.api.JavaVersion
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.reflect.KClass


fun Project.isRootProject(): Boolean {
    return this == rootProject
}


fun Project.doOn(
    vararg pluginClass: KClass<out Plugin<*>>,
    action: Plugin<*>.() -> Unit
) {
    val plugin = pluginClass.mapNotNull { plugins.findPlugin(it) }.firstOrNull()
    if (plugin != null) {
        action(plugin)
        return
    }
    plugins.whenPluginAdded {
        if (pluginClass.any { it.isInstance(this) }) {
            action(this)
            return@whenPluginAdded
        }
    }
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
                    initProject(this)
                }
            } else {
                initProject(this)
            }
        }
    }

    private fun Project.getProperty(key: String): String {
        return rootProject.extra.run {
            runCatching {
                this[key].toString()
            }.getOrElse { "" }
        }
    }


    private fun initProject(project: Project) {

        with(project) {
            tasks.withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8.toString()
                }
            }
            doOn(AppPlugin::class, LibraryPlugin::class) {
                android.apply {
                    compileSdkVersion(Versions.compileSdk)
                    lintOptions {
                        isAbortOnError = true
                        textReport = true
                        textOutput("stdout")
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_1_8
                        targetCompatibility = JavaVersion.VERSION_1_8
                    }
                }
            }
            if (BinaryInfo.valid()) {
                tasks.withType<Javadoc> {
                    options.encoding = "UTF-8"
                }

                afterEvaluate {
                    publishInfo?.let {
                        plugins.apply("com.github.panpf.bintray-publish")
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
                            autoPublish = false
                            website = "https://github.com/x930073498/component"
                        }

                        plugins.apply("maven-publish")
                        configure<PublishingExtension> {
                            this.repositories {
                                maven(uri("../repository"))
                            }
                        }

                    }
                }
            }
        }
    }
}