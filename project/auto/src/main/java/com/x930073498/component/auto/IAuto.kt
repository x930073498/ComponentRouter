package com.x930073498.component.auto

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.reflect.typeOf


interface IAuto

interface IRegister {
    fun register()
}

interface IModuleRegister : IRegister
interface ISerializer {
    fun <T : Any> serialize(data: T): String

    fun <T : Any> deserialize(source: String, type: Type): T?
}

inline fun <reified T : Any> ISerializer.deserialize(source: String): T? {
    return deserialize(source, T::class.java)
}