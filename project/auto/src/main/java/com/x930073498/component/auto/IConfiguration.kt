package com.x930073498.component.auto


abstract class IConfiguration {

    companion object{
        val empty by lazy {
            object : IConfiguration() {
                override fun ConfigurationHandler.config() {

                }

            }
        }
    }
     fun register() {
        AutoConfiguration.register(this)
    }


    abstract fun ConfigurationHandler.config()
}
