package com.x930073498.component.router.action

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import com.x930073498.component.core.app
import com.x930073498.component.core.currentActivity
import java.lang.ref.WeakReference

class ContextHolder private constructor(
    private val application: Application = app,
    private val context: Context = currentActivity,
) {

    private val contextRef = WeakReference(context)
    fun getContext(): Context {
        return contextRef.get() ?: application
    }

    fun getPackageManager():PackageManager{
        return application.packageManager
    }

    fun getApplication(): Application {
        return application
    }

    fun getActivity(): Activity {
        val context = getContext()
        if (context is Activity) return context
        return scanContextActivity(context) ?: currentActivity
    }

    private fun scanContextActivity(context: Context): Activity? {
        var current = context
        if (current is Activity) return current
        return if (current is ContextWrapper) {
            current = current.baseContext
            scanContextActivity(current)
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContextHolder

        if (application != other.application) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = application.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    companion object {
        internal fun create(context: Context? = null): ContextHolder {
            return if (context == null) return ContextHolder()
            else ContextHolder(context = context)
        }
    }
}