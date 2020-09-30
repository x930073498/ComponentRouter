package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder


interface FragmentActionDelegate<T> : ActionDelegate<T> {

    fun inject(bundle: Bundle, fragment: T)

    suspend fun factory(): Factory<T>? {
        return null
    }

     interface Factory<T> {
       suspend fun create(contextHolder: ContextHolder, clazz: Class<T>, bundle: Bundle): T?
    }

}
