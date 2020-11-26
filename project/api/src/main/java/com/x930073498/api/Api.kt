package com.x930073498.api

class Api {


    fun getStub(id: String): Stub {
        return stubMap[id] ?: Stub.defaultStub
    }

    companion object {

        internal val stubMap = mutableMapOf<String, Stub>()
        fun register(stub: Stub) {

            stubMap[stub.getID()] = stub
        }
    }
}