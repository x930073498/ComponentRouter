package com.x930073498.router.action

import android.net.Uri
import com.x930073498.router.impl.ActionDelegate
import com.x930073498.router.util.authorityAndPath

object ActionCenter {
    private val map = mutableMapOf<String, ActionDelegate<*>>()


    fun register(actionDelegate: ActionDelegate<*>) {
        map[actionDelegate.key] = actionDelegate
    }


    internal fun getAction(url: String): ActionDelegate<*>? {
        val key = Uri.parse(url).authorityAndPath().toString()
        return map[key]
    }

    internal fun getAction(uri: Uri): ActionDelegate<*>? {
        val key = uri.authorityAndPath().toString()
        return map[key]
    }


}