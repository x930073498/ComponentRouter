package com.x930073498.component.router.coroutines

import java.util.concurrent.CopyOnWriteArrayList

internal class HandleTree(
    private val queue: CoroutineActionQueue,
    private val parent: HandleTree? = null,
    private val awaitHandle: DeferredResultAwaitHandle<*>
) : DisposableHandle, ActionHandle, StarterHandle {


    private val children = CopyOnWriteArrayList(arrayListOf<HandleTree>())


    private fun doOnChild(action: (HandleTree) -> Unit) {
        children.forEach(action)
    }

    init {
        parent?.apply {
            children.add(this@HandleTree)
            if (isDisposed()) {
                this@HandleTree.dispose()
                return@apply
            }
            if (isDisposeSafety) {
                this@HandleTree.disposeSafety()
                return@apply
            }
            sendAction {
                this.awaitHandle.await()
                this@HandleTree.queue.start()
            }
        }

    }

    override fun start(): StarterHandle {
        if (parent != null) {
            parent.start()
        } else {
            queue.start()
        }
        return this
    }

    override fun sendAction(action: ActionHandle.Action) {
        queue.sendAction(action)
    }

    override fun dispose() {
        parent?.apply {
            if (!isDisposed()) dispose()
        }
        if (!isDisposed())
            queue.dispose()
        if (!awaitHandle.isDisposed())
            awaitHandle.dispose()
        doOnChild {
            if (it !== this && !it.isDisposed())
                it.dispose()
        }
    }

    private var isDisposeSafety = false
    override fun disposeSafety() {
        if (isDisposeSafety) return
        isDisposeSafety = true
        if (parent != null && !parent.isDisposeSafety) {
            parent.disposeSafety()
        }
        queue.disposeSafety()
        awaitHandle.disposeSafety()
        doOnChild {
            if (!it.isDisposeSafety) {
                it.disposeSafety()
            }
        }

    }

    override fun isDisposed(): Boolean {
        return queue.isDisposed()
    }

    override fun isStarted(): Boolean {
        return queue.isStarted()
    }


}