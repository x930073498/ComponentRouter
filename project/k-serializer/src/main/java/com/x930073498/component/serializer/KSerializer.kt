package com.x930073498.component.serializer

import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.IModuleRegister
import com.x930073498.component.auto.ISerializer
import com.x930073498.component.auto.setSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.reflect.Type


class KSerializer : ISerializer, IModuleRegister, IAuto {
    override fun <T : Any> serialize(data: T): String {
        return Json.encodeToString(Json.serializersModule.serializer(data.javaClass), data)
    }

    override fun <T : Any> deserialize(source: String, type: Type): T {
        return Json.decodeFromString(Json.serializersModule.serializer(type), source) as T
    }

    override fun register() {
        setSerializer(this)
    }

}