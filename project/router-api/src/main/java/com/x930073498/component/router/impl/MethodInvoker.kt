@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router.impl

interface MethodInvoker {
    suspend fun invoke(): Any?
}

