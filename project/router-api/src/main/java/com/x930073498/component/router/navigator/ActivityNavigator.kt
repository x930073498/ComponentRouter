package com.x930073498.component.router.navigator

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.navigator.impl.ActivityNavigatorImpl

interface ActivityNavigator : Navigator {

    companion object {
        internal fun create(
            listenable: ResultListenable<ActivityNavigatorParams>,
            navigatorOption: NavigatorOption,
        ): ActivityNavigator {
            val activityNavigatorOption =
                navigatorOption as? NavigatorOption.ActivityNavigatorOption
                    ?: NavigatorOption.ActivityNavigatorOption()
            return ActivityNavigatorImpl(listenable, activityNavigatorOption)
        }
    }

    fun getLaunchIntent(): ResultListenable<Intent?>

    fun navigateForActivityResult(activity: Activity): ResultListenable<ActivityResult>


    fun requestActivity(): ResultListenable<Activity?>

    fun <T : Activity> requestInstanceActivity(clazz: Class<T>): ResultListenable<T>

}

