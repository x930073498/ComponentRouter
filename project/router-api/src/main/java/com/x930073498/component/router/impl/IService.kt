package com.x930073498.component.router.impl

import android.os.Bundle
import com.x930073498.component.router.action.ContextHolder

interface IService {
    suspend fun init(contextHolder: ContextHolder, bundle: Bundle) {

    }

    suspend fun invoke() {

    }
}