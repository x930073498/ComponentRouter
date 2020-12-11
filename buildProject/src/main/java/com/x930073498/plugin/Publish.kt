package com.x930073498.plugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

object Publish {
    const val GROUP = "com.x930073498.component"
    const val VERSION = "0.0.6"
    enum class PublishInfo(
        val group: String,
        val artifact: String,
        val version: String,
        val desc: String
    ) {


        AUTO(GROUP, Name.AUTO, VERSION, Desc.AUTO_DESC),
        CORE(GROUP, Name.CORE, VERSION, Desc.CORE_DESC),
        AUTO_PLUGIN(
            GROUP,
            Name.AUTO_PLUGIN,
            VERSION,
            Desc.AUTO_PLUGIN_DESC
        ),
        AUTO_STARTER_DISPATCHER(
            GROUP,
            Name.AUTO_STARTER_DISPATCHER,
            VERSION,
            Desc.AUTO_STARTER_DISPATCHER_DESC
        ),
        ROUTER_ANNOTATIONS(
            GROUP,
            Name.ROUTER_ANNOTATIONS,
            VERSION,
            Desc.ROUTER_ANNOTATIONS_DESC
        ),
        ROUTER_API(GROUP, Name.ROUTER_API, VERSION, Desc.ROUTER_API_DESC),
        ROUTER_COMPILER(
            GROUP,
            Name.ROUTER_COMPILER,
            VERSION,
            Desc.ROUTER_COMPILER_DESC
        ),
        STARTER_DISPATCHER(
            GROUP,
            Name.STARTER_DISPATCHER,
           VERSION,
            Desc.STARTER_DISPATCHER_DESC
        );
        fun toDependency():String{
            return "$group:$artifact:$version"
        }

        override fun toString(): String {
            return "$name{$group,$artifact,$version,$desc}"
        }
    }



    @JvmStatic
    fun getPublishInfo(project: Project): PublishInfo? {
        return PublishInfo.values().firstOrNull { it.artifact == project.name }?.apply {
            println("PublishInfo=$this")
            project.extra.let {
                it["group"] = group
                it["artifact"] = artifact
                it["version"] = version
                it["desc"] = desc
            }
        }
    }


    object Name {
        const val AUTO = "auto"
        const val CORE = "core"
        const val AUTO_PLUGIN = "auto-plugin"
        const val AUTO_STARTER_DISPATCHER = "auto-starter-dispatcher"
        const val ROUTER_ANNOTATIONS = "router-annotations"
        const val ROUTER_API = "router-api"
        const val ROUTER_COMPILER = "router-compiler"
        const val STARTER_DISPATCHER = "starter-dispatcher"
    }


    object Desc {
        const val AUTO_DESC = "自动任务基础库"
        const val CORE_DESC = "自动任务核心库"
        const val AUTO_PLUGIN_DESC = "自动任务注入框架"
        const val AUTO_STARTER_DISPATCHER_DESC = "用于starter自动注入任务"
        const val ROUTER_ANNOTATIONS_DESC = "路由注解库"
        const val ROUTER_API_DESC = "路由api库"
        const val ROUTER_COMPILER_DESC = "路由注解processor"
        const val STARTER_DISPATCHER_DESC = "app启动器-starter"
    }
}

val Project.publishInfo: Publish.PublishInfo?
    get() = Publish.getPublishInfo(this)
