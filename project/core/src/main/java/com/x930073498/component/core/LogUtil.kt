package com.x930073498.component.core

import android.util.Log

object LogUtil {
    internal var debug = true
    fun log(msg: Any?) {
        if (msg == null) return
        if (debug) {
            Log.i("x930073498-component", msg.toString())
        }
    }

}