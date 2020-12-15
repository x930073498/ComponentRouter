package com.x930073498.component.router.impl

import com.x930073498.component.router.action.NavigateParams

interface ResultHandler {
    suspend fun handle(result: Any?, params: NavigateParams): Any?

    companion object {
        val Direct = object : ResultHandler {
            override suspend fun handle(result: Any?, params: NavigateParams): Any? {
                return result
            }

        }
    }
}