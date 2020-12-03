package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target

interface MethodActionDelegate: ActionDelegate {

    suspend fun factory(): Factory

     suspend fun target(): Target.MethodTarget

     interface Factory{
      suspend  fun create(contextHolder: ContextHolder, clazz: Class<*>, bundle: Bundle): MethodInvoker
    }

}