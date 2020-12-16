package com.x930073498.component.serializer

import com.google.gson.Gson
import com.x930073498.component.auto.*
import java.lang.reflect.Type


class GSerializer : ISerializer, IModuleRegister, IAuto {
    private val gson = Gson()
    override fun <T : Any> serialize(data: T): String {
        return gson.toJson(data)
    }

    override fun <T : Any> deserialize(source: String, type: Type): T? {
        return gson.fromJson(source,type)
    }

    override fun register() {
        setSerializer(this)
    }

}