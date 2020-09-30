package com.x930073498.api

interface Stub {
    fun invoke(vararg params: Any?)
    fun getID(): String
    suspend fun get() {

    }

    companion object {
        val defaultStub = object : Stub {
            override fun invoke(vararg params: Any?) {
            }

            override fun getID(): String {
                return ""
            }

        }
    }
}