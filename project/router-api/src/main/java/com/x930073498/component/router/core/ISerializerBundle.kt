package com.x930073498.component.router.core

import android.os.Bundle
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.router.util.ParameterSupport

interface ISerializerBundle {
    fun put(key: String, value: Any?)

    fun put(bundle: Bundle) {
        bundle.keySet().forEach {
            put(it, bundle.get(it))
        }
    }

    fun clear()


}