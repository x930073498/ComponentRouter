package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder

interface ServiceActionDelegate<T> : ActionDelegate<T> {
    fun inject(bundle: Bundle, provider: T)

   suspend fun factory(): Factory<T>? {
        return null
    }
    fun autoInvoke():Boolean

     interface Factory<T> {
       suspend fun create(contextHolder: ContextHolder, clazz: Class<T>, bundle: Bundle): T?
    }

}