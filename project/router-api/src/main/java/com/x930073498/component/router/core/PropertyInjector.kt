package com.x930073498.component.router.core

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

const val AUTO_INJECT_KEY = "c70b84c3-bef7-4598-b4c1-164c28faa355"

interface PropertyInjector {
    fun inject(activity: Activity, bundle: Bundle?)
    fun inject(fragment: Fragment, bundle: Bundle?)
}

fun PropertyInjector.inject(activity: Activity, intent: Intent?) {
    inject(activity, intent?.extras)
}

fun PropertyInjector.injectByIntent(activity: Activity) {
    inject(activity, activity.intent)
    activity.intent?.putExtra(AUTO_INJECT_KEY, true)
}

fun PropertyInjector.injectByArguments(fragment: Fragment) {
    inject(fragment, fragment.arguments)
    fragment.arguments?.putBoolean(AUTO_INJECT_KEY, true)
}

fun Activity.hasPropertyAutoInjectByRouter() = intent?.hasExtra(AUTO_INJECT_KEY) ?: false
fun Fragment.hasPropertyAutoInjectByRouter() = arguments?.get(AUTO_INJECT_KEY) != null
