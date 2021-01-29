package com.x930073498.plugin

import com.android.build.gradle.internal.plugins.AppPlugin
import com.android.build.gradle.internal.plugins.LibraryPlugin
import com.github.panpf.bintray.publish.BintrayConfiguration
import com.github.panpf.bintray.publish.BintrayPublishPlugin
import com.github.panpf.bintray.publish.PublishExtension
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import com.x930073498.Versions
import org.gradle.api.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import kotlin.reflect.KClass

private val gson = Gson().newBuilder()
    .setLenient()
    .addDeserializationExclusionStrategy(object :
        ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return f?.name == "project"
        }

        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return clazz == Project::class.java
        }
    }).create()


fun BintrayUploadTask.string(): String {
    return "BintrayUploadTask{" +
            "apiUrl='" + apiUrl + '\'' +
            ", user='" + user + '\'' +
            ", apiKey='" + apiKey + '\'' +
            ", configurations=" + Arrays.toString(configurations) +
            ", publications=" + Arrays.toString(publications) +
            ", filesSpec=" + filesSpec +
            ", publish=" + publish +
            ", override=" + override +
            ", dryRun=" + dryRun +
            ", userOrg='" + userOrg + '\'' +
            ", repoName='" + repoName + '\'' +
            ", packageName='" + packageName + '\'' +
            ", packageDesc='" + packageDesc + '\'' +
            ", packageWebsiteUrl='" + packageWebsiteUrl + '\'' +
            ", packageIssueTrackerUrl='" + packageIssueTrackerUrl + '\'' +
            ", packageVcsUrl='" + packageVcsUrl + '\'' +
            ", packageGithubRepo='" + packageGithubRepo + '\'' +
            ", packageGithubReleaseNotesFile='" + packageGithubReleaseNotesFile + '\'' +
            ", packageLicenses=" + Arrays.toString(packageLicenses) +
            ", packageLabels=" + Arrays.toString(packageLabels) +
            ", packageAttributes=" + packageAttributes +
            ", packagePublicDownloadNumbers=" + packagePublicDownloadNumbers +
            ", debianDistribution='" + debianDistribution + '\'' +
            ", debianComponent='" + debianComponent + '\'' +
            ", debianArchitecture='" + debianArchitecture + '\'' +
            ", versionName='" + versionName + '\'' +
            ", versionDesc='" + versionDesc + '\'' +
            ", versionReleased='" + versionReleased + '\'' +
            ", signVersion=" + signVersion +
            ", gpgPassphrase='" + gpgPassphrase + '\'' +
            ", versionVcsTag='" + versionVcsTag + '\'' +
            ", versionAttributes=" + versionAttributes +
            ", syncToMavenCentral=" + syncToMavenCentral +
            ", ossUser='" + ossUser + '\'' +
            ", ossPassword='" + ossPassword + '\'' +
            ", ossCloseRepo='" + ossCloseRepo + '\'' +
            ", configurationUploads=" + Arrays.toString(configurationUploads) +
            ", publicationUploads=" + Arrays.toString(publicationUploads) +
            ", fileUploads=" + Arrays.toString(fileUploads) +
            '}'

}

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
            gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {
                override fun beforeEvaluate(project: Project) {
                    if (project.isRootProject()) return
                    initProject(project)
                    applyBinaryPlugin(project)
                }

                override fun afterEvaluate(project: Project, state: ProjectState) {
//                    if (project.isRootProject()) return
//                    applyBinaryPlugin(project)
                }

            })
        }
    }

    private fun Project.getProperty(key: String): String {
        return rootProject.extra.run {
            runCatching {
                this[key].toString()
            }.getOrElse { "" }
        }
    }


    private fun Project.setBaseOptions() {
        android.apply {
            defaultConfig {
                minSdkVersion(Versions.minSdk)
                targetSdkVersion(Versions.targetSdk)
                versionCode(Versions.versionCode)
                versionName(Versions.versionName)
                consumerProguardFile("consumer-rules.pro")
            }
            buildTypes {
                getByName("release") {
                    minifyEnabled(false)
                }
            }
            compileSdkVersion(Versions.compileSdk)
            lintOptions {
                disable("GoogleAppIndexingWarning")
                baseline(file("lint-baseline.xml"))
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

    private fun applyBinaryPlugin(project: Project) {
        with(project) {
            if (BinaryInfo.valid()) {
                this.publishInfo?.let {
                    group=it.group
                    version=it.version
                    description=it.desc
                    plugins.apply(BintrayPublishPlugin::class)
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
                    configure<PublishingExtension> {
                        this.repositories {
                            maven(uri("../repository"))
                        }
                    }
                }
            }
        }
    }

    private fun initProject(project: Project) {
        with(project) {
            tasks.withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8.toString()
                }
            }
            tasks.withType<JavaCompile> {
                options.encoding = "UTF-8"
            }
            tasks.withType<GenerateModuleMetadata> {
                enabled = false
            }
            tasks.withType<Javadoc> {
                options.encoding = "UTF-8"
            }
            plugins.withType<AppPlugin> {
                setBaseOptions()
            }
            plugins.withType<LibraryPlugin> {
                setBaseOptions()
            }


        }
    }
}