package com.x930073498.router.action

import android.app.Application
import android.content.Context
import com.x930073498.router.Router
import java.lang.ref.WeakReference

class ContextHolder private constructor(
    private val application: Application = Router.app,
    private val context: Context = application,
) {
    private val contextRef = WeakReference(context)
    fun getContext(): Context {
        return contextRef.get() ?: application
    }

    fun getApplication(): Application {
        return application
    }

    companion object {
        internal fun create(context: Context?): ContextHolder {
            return if (context == null) return ContextHolder()
            else ContextHolder(context = context)
        }
    }
}