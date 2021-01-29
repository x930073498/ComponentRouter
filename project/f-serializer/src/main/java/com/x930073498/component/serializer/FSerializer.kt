package com.x930073498.component.serializer

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.parser.Feature
import com.x930073498.component.auto.*
import java.lang.reflect.Type


class FSerializer : ISerializer, IModuleRegister, IAuto {
    override fun <T : Any> serialize(data: T): String {
        return JSON.toJSONString(data)
    }

    override fun <T : Any> deserialize(source: String, type: Type): T? {
        return JSON.parseObject<T>(source, type, Feature.AllowUnQuotedFieldNames)
    }

    override fun register() {
        ConfigurationHolder.byDefault {
            setSerializer(this@FSerializer)
        }
    }

}