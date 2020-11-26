package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder

interface MethodInvoker<T> {

    
   suspend fun invoke(contextHolder: ContextHolder, bundle: Bundle):T?


}