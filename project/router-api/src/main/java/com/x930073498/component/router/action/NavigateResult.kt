package com.x930073498.component.router.action

data class NavigateResult(val result: Any?)  {

    companion object {
        val empty = NavigateResult(null)

        fun success(result: Any?) = NavigateResult(result)
    }
}

internal fun Any?.asResult() =
    if (this == null) NavigateResult.empty
    else NavigateResult.success(this)
