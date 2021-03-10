package com.x930073498.component.router.coroutines

interface ActionHandle : DisposableHandle {

    fun sendAction(action: ActionHandle.Action)

    sealed class Action {
        internal class SuspendRunnableAction(val runnable: ActionHandle.SuspendRunnable) : ActionHandle.Action()
    }

    interface SuspendRunnable {
        suspend fun run()
    }
}

internal fun ActionHandle.sendAction(block: suspend () -> Unit) {
    sendAction(SuspendRunnable(block).asAction())
}

internal fun ActionHandle.SuspendRunnable.asAction(): ActionHandle.Action {
    return com.x930073498.component.router.coroutines.ActionHandle.Action.SuspendRunnableAction(this)
}


@Suppress("FunctionName")
internal fun SuspendRunnable(block: suspend () -> Unit) =
    object : ActionHandle.SuspendRunnable {
        override suspend fun run() {
            block()
        }

    }