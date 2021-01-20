package com.x930073498.component.router.util

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.MainThread
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.coroutines.ResultSetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume


private var requestCode = 60000

@Synchronized
private fun getRequestCode(): Int {
    return requestCode--.apply {
        if (this <= 100) requestCode = 60000
    }
}


class OnActivityResultFragment : Fragment() {

    private var isCreated = false
    private var createAction: (OnActivityResultFragment) -> Unit = {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCreated = true
        createAction(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        isCreated = false
    }


    fun onCreated(action: (OnActivityResultFragment) -> Unit) {
        if (isCreated) action(this)
        else {
            this.createAction = action
        }
    }

    private var callback: (Int, ActivityResult) -> Unit = { _, _ -> }
    fun setResultCallback(callback: (requestCode: Int, result: ActivityResult) -> Unit) {
        this.callback = callback
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callback.invoke(requestCode, ActivityResult(resultCode, data))
    }
}


@MainThread
internal fun launchAndWaitActivityResult(
    activity: Activity,
    key: String,
    launchIntent: Intent?,
    setter: ResultSetter<ActivityResult>
) {
    if (activity is ActivityResultRegistryOwner) {
        launchAndWaitActivityResultByActivityResultRegistryOwner(
            activity,
            key,
            launchIntent,
            setter
        )
    } else {
        val requestCode = getRequestCode()
        launchAndWaitActivityResultByFragment(activity, requestCode, key, launchIntent,setter)
    }
}


private fun createResultContract(): ActivityResultContract<Intent, ActivityResult> {
    return object : ActivityResultContract<Intent, ActivityResult>() {
        override fun createIntent(context: Context, input: Intent): Intent {
            return input
        }

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
            return ActivityResult(resultCode, intent)
        }

    }
}

private fun createResultCallback(setter: ResultSetter<ActivityResult>): ActivityResultCallback<ActivityResult> {
    return ActivityResultCallback<ActivityResult> { result -> setter.setResult(result) }
}


@MainThread
private fun launchAndWaitActivityResultByActivityResultRegistryOwner(
    activityResultRegistryOwner: ActivityResultRegistryOwner,
    key: String,
    launchIntent: Intent?,
    setter: ResultSetter<ActivityResult>
) {
    if (launchIntent == null) {
        setter.setResult(ActivityResult(Activity.RESULT_CANCELED, null))
        return
    }
    val contract = createResultContract()
    val callback = createResultCallback(setter)
    activityResultRegistryOwner.activityResultRegistry.register(key, contract, callback)
        .launch(launchIntent)
}

private fun createOrFindFragment(activity: Activity, tag: String): OnActivityResultFragment {
    val manager = activity.fragmentManager
    var fragment = manager.findFragmentByTag(tag)
    if (fragment is OnActivityResultFragment) return fragment
    fragment = OnActivityResultFragment()
    manager.beginTransaction().add(fragment, tag).commit()
    return fragment
}

private fun launchAndWaitActivityResultByFragment(
    activity: Activity,
    mRequestCode: Int,
    key: String,
    launchIntent: Intent?,
    setter: ResultSetter<ActivityResult>
) {
    if (launchIntent == null) {
        setter.setResult(ActivityResult(Activity.RESULT_CANCELED, null))
        return
    }

    val fragment = createOrFindFragment(activity, key)
    fragment.setResultCallback { requestCode, result ->
        if (requestCode == mRequestCode) {
            setter.setResult(result)
            activity.fragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fragment.onCreated {
        it.startActivityForResult(launchIntent, mRequestCode)
    }
}
