package com.x930073498.component.core

object AutoConfiguration {

    internal fun init() {
        IConfiguration.handle()
    }


    private var checkRouterUnique = false

    fun shouldRouterUnique(): Boolean {
        return checkRouterUnique
    }

    fun debug() {
        AutoTaskRegister.debugable = true
    }

    fun checkRouteUnique() {
        checkRouterUnique = true
    }
}