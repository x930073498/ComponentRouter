package com.x930073498.component.auto.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.x930073498.component.auto.plugin.options.Options
import com.x930073498.component.auto.plugin.options.RouterOptions
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.util.ConfigureUtil

open class AutoOptions constructor(
    var enableDependency: Boolean = true,
    var enableDispatcher: Boolean = true,
    var enable: Boolean = true,
    var enableFragmentation: Boolean = true,
    var serializer: String = SerializerType.NONE.name,
    var mavenUrl: String = "",
    var versionPattern: String = "+",
    var enableLog: Boolean = false
) {
    private val optionList = arrayListOf<Options>()

    fun <T> action(clazz: Class<T>, action: Action<T>) where T : Options {
        val target = clazz.newInstance()
        action.execute(target)
        optionList.add(target)
    }

    inline fun <reified T> action(action: Action<T>) where T : Options {
        action(T::class.java, action)
    }

    fun <T> action(clazz: Class<T>, action: Closure<T>) where T : Options {
        optionList.add(ConfigureUtil.configure(action, clazz.newInstance()))
    }

    inline fun <reified T> action(action: Closure<T>) where T : Options {
        action(T::class.java, action)
    }


    fun router(action: Closure<RouterOptions>) {
        this.action(action)
    }

    fun router(action: Action<RouterOptions>) {
        this.action(action)
    }


    private fun isValid(options: AutoOptions) = options.enable
    private fun apply(project: Project, options: AutoOptions) {
        if (!isValid(options)) {
            return
        }
        project.allprojects {
            if (this == rootProject) return@allprojects

            if (!options.enable) return@allprojects
            plugins.whenPluginAdded {
                if (this is AppPlugin) {
                    android.registerTransform(ASMTransform(this@allprojects))
                    setDependency(this, options)
                } else if (this is LibraryPlugin || this is JavaLibraryPlugin) {
                    setDependency(this, options)
                }
                options.optionList.forEach {
                    it.apply(this@allprojects, this, options)
                }
            }
        }

    }

    internal fun apply(project: Project) {
        apply(project, this)
    }
}