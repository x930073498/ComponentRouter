package com.x930073498.component.serializer

import com.squareup.moshi.Moshi
import com.x930073498.component.auto.*
import java.lang.reflect.Type


class MSerializer : ISerializer, IModuleRegister, IAuto {
    private val moshi = Moshi.Builder().build()
    override fun <T : Any> serialize(data: T): String {
        return moshi.adapter(data.javaClass).toJson(data)
    }

    override fun <T : Any> deserialize(source: String, type: Type): T? {

        return moshi.adapter<T>(type).fromJson(source)
    }

    override fun register() {
        setSerializer(this)
    }

}