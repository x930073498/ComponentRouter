package com.x930073498.component.router.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CoroutineActionQueue(
    internal val scope: CoroutineScope,
    internal val context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.LAZY
) : DisposableHandle, ActionHandle, StarterHandle {

    private var isStarted = false

    private val channel = Channel<ActionHandle.Action>(Channel.UNLIMITED)

    private var isDisposeByUser = false

    private val job = scope.launch(context, start) {
        isStarted = true
        for (action in channel) {
            when (action) {
                is ActionHandle.Action.SuspendRunnableAction -> action.runnable.run()
            }
        }
    }


    override fun sendAction(action: ActionHandle.Action) {
        if (isDisposed()) return
        channel.offer(action)
    }

    override fun start(): CoroutineActionQueue {
        if (isStarted) return this
        job.start()
        isStarted = true
        return this
    }

    override fun isStarted(): Boolean {
        return isStarted
    }


    override fun dispose() {
        if (isDisposeByUser) return
        if (isDisposed()) return
        isDisposeByUser = true
        job.cancel()
        channel.cancel()
    }

    override fun disposeSafety() {
        sendAction {
            dispose()
        }
    }


    override fun isDisposed(): Boolean {
        return isDisposeByUser || job.isCompleted
    }


}