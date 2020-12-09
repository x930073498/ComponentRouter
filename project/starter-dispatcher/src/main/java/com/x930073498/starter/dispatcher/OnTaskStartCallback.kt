package com.x930073498.starter.dispatcher

import com.x930073498.starter.task.AppStartTask


internal interface OnTaskStartCallback {
    fun beforeStart(dispatcher: AppStartTaskDispatcher,list: List<AppStartTask>)
}