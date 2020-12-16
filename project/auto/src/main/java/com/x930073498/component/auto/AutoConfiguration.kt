package com.x930073498.component.auto

interface ConfigurationHandler {
    fun debug()
    fun checkRouteUnique()
    fun setSerializer(serializer: ISerializer)
}

interface ConfigurationResult {
    fun isDebug(): Boolean
    fun shouldRouterUnique(): Boolean
    fun getSerializer(): ISerializer?
}

internal val configuration by lazy {
    AutoConfiguration()
}


fun getConfiguration():ConfigurationResult{
    return configuration
}
fun applyConfiguration(action: ConfigurationResult.() -> Unit) {
    AutoConfiguration.configuration.apply {
        configuration.config()
    }
    LogUtil.debug = configuration.isDebug()
    action(configuration)
}

fun setSerializer(serializer: ISerializer){
    configuration.setSerializer(serializer)
}

internal class AutoConfiguration : ConfigurationResult, ConfigurationHandler {
    companion object {
        var configuration: IConfiguration  = IConfiguration.empty
        fun register(configuration: IConfiguration) {
            if (this.configuration == IConfiguration.empty) {
                this.configuration = configuration
            } else {
                LogUtil.log("已经定义configuration className=${configuration.javaClass},当前configuration className=${this.configuration?.javaClass}无效")
            }
        }


    }

    private var serializer: ISerializer? = null
    private var isDebug = false

    override fun getSerializer(): ISerializer? {
        return serializer
    }


    private var checkRouterUnique = false

    override fun shouldRouterUnique(): Boolean {
        return checkRouterUnique
    }

    override fun debug() {
        isDebug = true
    }

    override fun isDebug(): Boolean {
        return isDebug
    }

    override fun setSerializer(serializer: ISerializer) {
        if (this.serializer != null) return
        this.serializer = serializer
    }

    override fun checkRouteUnique() {
        checkRouterUnique = true
    }
}