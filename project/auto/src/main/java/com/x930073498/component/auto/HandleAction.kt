package com.x930073498.component.auto

interface Action
interface HandleAction : Action, IAuto {
    fun setDebug(debug: Boolean)

    fun setSerializer(serializer: ISerializer)

    fun setLogger(logger: Logger)
}