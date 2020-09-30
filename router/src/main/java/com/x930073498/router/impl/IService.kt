package com.x930073498.router.impl

import android.os.Bundle
import com.x930073498.router.action.ContextHolder

interface IService {
    suspend fun init(contextHolder: ContextHolder, bundle: Bundle) {

    }

    suspend fun invoke() {

    }
}