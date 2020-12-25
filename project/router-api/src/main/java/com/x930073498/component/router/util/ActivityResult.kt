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
import com.x930073498.component.core.isMainThread
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

    var isCreated = false

    private val channel = Channel<Boolean>()
    suspend fun waitCreated(): OnActivityResultFragment {
        if (isCreated) return this
        while (!isCreated) {
            select<Boolean> {
                channel.onReceive {
                    it
                }
            }
        }
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCreated = true
        channel.offer(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        isCreated = false
    }

    private var callback: (Int, ActivityResult) -> Unit = { _, _ -> }
    fun setResultCallback(callback: (requestCode: Int, result: ActivityResult) -> Unit) {
        this.callback = callback
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callback.invoke(requestCode, ActivityResult(resultCode, data))
    }


}


internal suspend fun launchAndWaitActivityResult(
    activity: Activity,
    key: String,
    launchIntent: Intent
): ActivityResult {
    return if (activity is ActivityResultRegistryOwner) {
        launchAndWaitActivityResultByActivityResultRegistryOwner(
            activity,
            key,
            launchIntent
        )
    } else {
        val requestCode = getRequestCode()
        launchAndWaitActivityResultByFragment(activity, requestCode, key, launchIntent)
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

private fun createResultCallback(continuation: Continuation<ActivityResult>): ActivityResultCallback<ActivityResult> {
    return ActivityResultCallback<ActivityResult> { result -> continuation.resume(result) }
}


private suspend fun launchAndWaitActivityResultByActivityResultRegistryOwner(
    activityResultRegistryOwner: ActivityResultRegistryOwner,
    key: String,
    launchIntent: Intent
): ActivityResult {
    val block = suspend {
        suspendCancellableCoroutine<ActivityResult> {
            val contract = createResultContract()
            val callback = createResultCallback(it)
            activityResultRegistryOwner.activityResultRegistry.register(key, contract, callback)
                .launch(launchIntent)
        }
    }
    return if (isMainThread) {
        block()
    } else {
        withContext(Dispatchers.Main.immediate) {
            block()
        }
    }

}

private fun createOrFindFragment(activity: Activity, tag: String): OnActivityResultFragment {
    val manager = activity.fragmentManager
    var fragment = manager.findFragmentByTag(tag)
    if (fragment is OnActivityResultFragment) return fragment
    fragment = OnActivityResultFragment()
    manager.beginTransaction().add(fragment, tag).commit()
    return fragment
}

private suspend fun launchAndWaitActivityResultByFragment(
    activity: Activity,
    mRequestCode: Int,
    key: String,
    launchIntent: Intent
): ActivityResult {
    val block = suspend {
        val fragment = createOrFindFragment(activity, key)
        fragment.waitCreated()
        suspendCancellableCoroutine<ActivityResult> {
            fragment.setResultCallback { requestCode, result ->
                if (requestCode == mRequestCode) {
                    it.resume(result)
                }
            }
            fragment.startActivityForResult(launchIntent, mRequestCode)
        }
    }
    return if (isMainThread) {
        block()
    } else {
        withContext(Dispatchers.Main.immediate) {
            block()
        }
    }

}
