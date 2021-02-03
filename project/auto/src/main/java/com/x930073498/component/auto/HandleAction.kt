package com.x930073498.component.auto

interface Action

val Action.serializer: ISerializer?
    get() {
        return iSerializer
    }

interface HandleAction : Action, IAuto {
    fun setDebug(debug: Boolean)

    fun setSerializer(serializer: ISerializer)

    fun setLogger(logger: Logger)
}