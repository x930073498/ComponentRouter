package com.x930073498.component.serializer

import com.alibaba.fastjson.JSON
import com.x930073498.component.auto.*
import java.lang.reflect.Type


class FSerializer : ISerializer, IModuleRegister, IAuto {
    override fun <T : Any> serialize(data: T): String {
        return JSON.toJSONString(data)
    }

    override fun <T : Any> deserialize(source: String, type: Type): T? {
        return JSON.parseObject(source,type)
    }

    override fun register() {
        setSerializer(this)
    }

}