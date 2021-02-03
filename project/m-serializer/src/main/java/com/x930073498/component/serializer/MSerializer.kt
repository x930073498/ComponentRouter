package com.x930073498.component.serializer

import com.squareup.moshi.Moshi
import com.x930073498.component.auto.*
import java.lang.reflect.Type


class MSerializer : ISerializer, IModuleRegister, IAuto {
    private val moshi = Moshi.Builder().build()
    override fun <T> serialize(data: T): String {
        if (data == null) return ""
        return moshi.adapter(data.javaClass).toJson(data)
    }

    override fun <T> deserialize(source: String, type: Type): T? {
        return moshi.adapter<T>(type).fromJson(source)
    }

    override fun register() {
        ConfigurationHolder.byDefault {
            setSerializer(this@MSerializer)
        }
    }

}