package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder

interface MethodActionDelegate<R,T>: ActionDelegate<T> where R: MethodInvoker<T> {

    fun factory(): Factory<R>? {
        return null
    }

     interface Factory<T> {
      suspend  fun create(contextHolder: ContextHolder, clazz: Class<T>, bundle: Bundle): T?
    }

}