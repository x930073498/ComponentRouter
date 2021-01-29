package com.x930073498.component.router.core

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.component.router.Router
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.impl.ActivityActionDelegate
import com.x930073498.component.router.impl.FragmentActionDelegate

class PropertyInjectorImpl:PropertyInjector {
    override fun inject(activity: Activity, bundle: Bundle?) {
        if (bundle == null) return
        val path = Router.ofHandle().getRealPathFromTarget(activity) ?: return
        val center = ActionCenter.getAction(path)
        (center as? ActivityActionDelegate)?.apply {
            inject(bundle, activity)
        }
    }

    override fun inject(fragment: Fragment, bundle: Bundle?) {
        if (bundle == null) return
        val path = Router.ofHandle().getRealPathFromTarget(fragment) ?: return
        val center = ActionCenter.getAction(path)
        (center as? FragmentActionDelegate)?.apply {
            inject(bundle, fragment)
        }
    }
}