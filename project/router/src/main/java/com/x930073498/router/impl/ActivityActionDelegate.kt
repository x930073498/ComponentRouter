package com.x930073498.router.impl

import android.content.Intent

interface ActivityActionDelegate<T> : ActionDelegate<T> {

    fun inject(intent: Intent, activity: T)

}