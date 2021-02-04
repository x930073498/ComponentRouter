package com.x930073498.component.router.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.x930073498.component.auto.IAuto
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.IApplicationLifecycle
import com.x930073498.component.core.IFragmentLifecycle
import com.x930073498.component.router.Router
import com.x930073498.component.router.activityPropertyAutoInject
import com.x930073498.component.router.fragmentPropertyAutoInject

class RouterInjectTask : IAuto, IActivityLifecycle, IApplicationLifecycle, IFragmentLifecycle {
    override fun onApplicationCreated(app: Application) {
    }

    private val activityInjectList = arrayListOf<Activity>()
    private val fragmentInjectedList = arrayListOf<Fragment>()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        autoInjectActivityProperty(activity)
        activityInjectList.add(activity)
    }

    private fun autoInjectActivityProperty(activity: Activity) {
        if (activityPropertyAutoInject) Router.injectByIntent(activity)
    }

    private fun autoInjectFragmentProperty(fragment: Fragment) {
        if (fragmentPropertyAutoInject) Router.injectByArguments(fragment)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (!activityInjectList.contains(activity)) {
            autoInjectActivityProperty(activity)
        } else {
            activityInjectList.remove(activity)
        }
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentPreCreated(fm, f, savedInstanceState)
        autoInjectFragmentProperty(f)
        fragmentInjectedList.add(f)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        if (!fragmentInjectedList.contains(f)) {
            autoInjectFragmentProperty(f)
        } else {
            fragmentInjectedList.remove(f)
        }
    }

}




