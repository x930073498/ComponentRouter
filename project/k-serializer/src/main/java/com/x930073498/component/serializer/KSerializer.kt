package com.x930073498.component.serializer

import com.x930073498.component.auto.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.reflect.Type


class KSerializer : ISerializer, IModuleRegister, IAuto {
    @ExperimentalSerializationApi
    override fun <T> serialize(data: T): String {
        if (data ==null)return "null"
        return Json.encodeToString(Json.serializersModule.serializer(data.javaClass), data)
    }

    @ExperimentalSerializationApi
    override fun <T> deserialize(source: String, type: Type): T {
        return Json.decodeFromString(Json.serializersModule.serializer(type), source) as T
    }

    override fun register() {
        ConfigurationHolder.byDefault {
            setSerializer(this@KSerializer)
        }
    }

}