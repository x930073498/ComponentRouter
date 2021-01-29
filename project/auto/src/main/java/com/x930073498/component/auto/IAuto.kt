package com.x930073498.component.auto

import java.lang.reflect.Type


interface IAuto{


}

interface IRegister:IAuto {
    fun register()
}

interface IModuleRegister : IRegister
interface ISerializer :IAuto{
    fun <T : Any> serialize(data: T): String

    fun <T : Any> deserialize(source: String, type: Type): T?
}

inline fun <reified T : Any> ISerializer.deserialize(source: String): T? {
    return deserialize(source, T::class.java)
}