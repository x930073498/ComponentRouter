package com.x930073498.component.auto

import androidx.annotation.RestrictTo

object ConfigurationHolder {
    private val configurations = arrayListOf<IConfiguration>()
    private val actions = arrayListOf<Action>()

    init {
        push(DefaultHandleAction())
    }

    private var hasApplied = false

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun register(configuration: IConfiguration) {
        if (hasApplied) return
        configurations.add(configuration)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun apply() {
        hasApplied = true
        configurations.forEach {
            it.option(this)
        }
        configurations.clear()
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun push(action: Action) {
        if (actions.any { it.javaClass == action.javaClass }) return
        actions.add(action)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : Action> getAction(clazz: Class<T>): T? {
        val action = actions.firstOrNull { clazz.isInstance(it) } ?: return null
        return clazz.cast(action)
    }

}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <reified T : Action> ConfigurationHolder.getAction(): T? {
    return getAction(T::class.java)
}

fun ConfigurationHolder.byDefault(action: HandleAction.() -> Unit) {
    val handle = getAction<HandleAction>() ?: return
    return action(handle)
}