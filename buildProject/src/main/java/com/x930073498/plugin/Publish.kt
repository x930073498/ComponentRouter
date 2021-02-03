package com.x930073498.plugin

import com.android.build.gradle.BaseExtension
import com.x930073498.plugin.Publish.GROUP
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
val Project.android: BaseExtension
    get() = project.extensions.getByName("android") as BaseExtension
object Publish {
    const val GROUP = "com.x930073498.component"
    const val VERSION = "0.0.23"

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
        ),
        FRAGMENTATION(GROUP, Name.FRAGMENTATION, VERSION, Desc.FRAGMENTATION),
        K_SERIALIZER(
            GROUP,
            Name.K_SERIALIZER,
            VERSION,
            Desc.K_SERIALIZER_DESC
        ),
        M_SERIALIZER(
            GROUP,
            Name.M_SERIALIZER,
            VERSION,
            Desc.M_SERIALIZER_DESC
        ),
        G_SERIALIZER(
            GROUP,
            Name.G_SERIALIZER,
            VERSION,
            Desc.G_SERIALIZER_DESC
        ),
        F_SERIALIZER(
            GROUP,
            Name.F_SERIALIZER,
            VERSION,
            Desc.F_SERIALIZER_DESC
        ),

        ;

        fun toDependency(): String {
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
        const val FRAGMENTATION = "fragmentation"
        const val K_SERIALIZER = "k-serializer"
        const val M_SERIALIZER = "m-serializer"
        const val G_SERIALIZER = "g-serializer"
        const val F_SERIALIZER = "f-serializer"
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
        const val K_SERIALIZER_DESC = "kotlin自带序列化实现"
        const val M_SERIALIZER_DESC = "moshi序列化实现"
        const val G_SERIALIZER_DESC = "gson序列化实现"
        const val F_SERIALIZER_DESC = "fastJson序列化实现"
        const val FRAGMENTATION = "基于router的navigation实现"
    }
}

val Project.publishInfo: Publish.PublishInfo?
    get() = Publish.getPublishInfo(this)





