package com.x930073498.component.starter.dispatcher

import com.x930073498.component.starter.task.AppStartTask


internal interface OnTaskStartCallback {
    fun beforeStart(dispatcher: AppStartTaskDispatcher,list: List<AppStartTask>)
}