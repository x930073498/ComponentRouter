package com.x930073498.component.router.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.getConfiguration
import com.x930073498.component.core.IActivityLifecycle
import com.x930073498.component.core.IApplicationLifecycle
import com.x930073498.component.core.IFragmentLifecycle
import com.x930073498.component.router.Router
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.impl.ActivityActionDelegate
import com.x930073498.component.router.impl.FragmentActionDelegate
import com.x930073498.component.router.util.ParameterSupport

class RouterInjectTask : IAuto, IActivityLifecycle, IApplicationLifecycle, IFragmentLifecycle {
    override fun onApplicationCreated(app: Application) {
        Router.init(app).apply {
            checkRouteUnique(getConfiguration().shouldRouterUnique())
        }
    }

    private val activityInjectList = arrayListOf<Activity>()
    private val fragmentInjectedList = arrayListOf<Fragment>()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        inject(activity)
        activityInjectList.add(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (!activityInjectList.contains(activity)) {
            inject(activity)
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
        inject(f)
        fragmentInjectedList.add(f)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        if (!fragmentInjectedList.contains(f)) {
            inject(f)
        } else {
            fragmentInjectedList.remove(f)
        }
    }

}

internal fun <T> inject(activity: T) where T : Activity {
    val intent = activity.intent ?: return
    val key = ParameterSupport.getCenterKey(intent) ?: return
    val center = ActionCenter.getAction(key)
    val bundle = intent.extras ?: return
    (center as? ActivityActionDelegate)?.apply {
        inject(bundle, activity)
    }
}

internal fun <T> inject(fragment: T) where T : Fragment {
    val bundle = fragment.arguments ?: return
    val key = ParameterSupport.getCenterKey(bundle) ?: return
    val center = ActionCenter.getAction(key)
    (center as? FragmentActionDelegate)?.apply {
        inject(bundle, fragment)
    }
}
