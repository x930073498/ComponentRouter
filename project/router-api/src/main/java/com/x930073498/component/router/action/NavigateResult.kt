package com.x930073498.component.router.action

import com.x930073498.component.router.interceptor.Response

data class NavigateResult(val result: Any?) : Response {

    companion object {
        val empty = NavigateResult(null)

        fun success(result: Any?) = NavigateResult(result)
    }
}

internal fun Any?.asResult() =
    if (this == null) NavigateResult.empty
    else NavigateResult.success(this)
