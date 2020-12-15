package com.x930073498.component.core

abstract class IConfiguration {
    companion object {
        internal var configuration: IConfiguration? = null

        fun handle() {
            configuration?.run {
                AutoConfiguration.config()
            }
        }
    }


    fun register() {
        if (configuration == null) {
            configuration = this
        } else {
            LogUtil.log("已经定义configuration className=${configuration?.javaClass},当前configuration className=${javaClass}无效")
        }
    }


    abstract fun AutoConfiguration.config()
}
