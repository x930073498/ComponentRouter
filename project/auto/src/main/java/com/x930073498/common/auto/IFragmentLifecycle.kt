package com.x930073498.common.auto

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface IFragmentLifecycle {
    fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
    }

    fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
    }

    fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
    }

    fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
    }

    fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
    }

    fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
    }

    fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
    }

    fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
    }

    fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
    }
}

val IFragmentLifecycle.app: Application
    get() {
        return FragmentLifecycle.get(this).app
    }

fun IFragmentLifecycle.register() {
    FragmentLifecycle.get(this).register()

}

private class FragmentLifecycle(val lifecycle: IFragmentLifecycle) :
    FragmentManager.FragmentLifecycleCallbacks() {
    private var hasRegister = false
    val app: Application

    init {
        map[lifecycle] = this
        app = AutoTaskRegister.app
    }

    fun register() {
        if (hasRegister) return
        AutoTaskRegister.activityLifecycle.add(this)
        hasRegister = true
    }

    companion object {
        private val map =
            mutableMapOf<IFragmentLifecycle, FragmentLifecycle>()

        internal fun get(lifecycle: IFragmentLifecycle): FragmentLifecycle {
            return map[lifecycle] ?: FragmentLifecycle(lifecycle)
        }
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        lifecycle.onFragmentPreAttached(fm, f, context)
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        lifecycle.onFragmentAttached(fm, f, context)
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        lifecycle.onFragmentPreCreated(fm, f, savedInstanceState)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        lifecycle.onFragmentCreated(fm, f, savedInstanceState)
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        lifecycle.onFragmentActivityCreated(fm, f, savedInstanceState)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        lifecycle.onFragmentViewCreated(fm, f, v, savedInstanceState)
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentStarted(fm, f)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentResumed(fm, f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentPaused(fm, f)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentStopped(fm, f)
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        lifecycle.onFragmentSaveInstanceState(fm, f, outState)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentViewDestroyed(fm, f)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentDestroyed(fm, f)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        lifecycle.onFragmentDetached(fm, f)
    }
}
