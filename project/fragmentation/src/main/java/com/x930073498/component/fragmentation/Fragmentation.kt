package com.x930073498.component.fragmentation

import android.view.Window
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.fragment.app.*
import androidx.fragment.app.getFragmentContainerId


fun Fragment.popChild() {

}

fun Fragment.popSelf() {

}


infix fun Fragment.launch(info: FragmentLaunchInfo) {
    info launchIn this
}

fun FragmentActivity.pop() {

}

fun FragmentActivity.launch(info: FragmentLaunchInfo) {
    info launchIn this
}


sealed class LaunchMode {
    object SingleTask : LaunchMode()
    object SingleTop : LaunchMode()
    object Standard : LaunchMode()
}

class FragmentLaunchInfo(
    val fragment: Fragment,
    val launchMode: LaunchMode = LaunchMode.Standard
) {


    fun launchIn(
        fragmentManager: FragmentManager,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        containerId: Int,
    ) {
        when (launchMode) {
            LaunchMode.SingleTask -> {

            }
            LaunchMode.SingleTop -> {

            }
            LaunchMode.Standard -> {
                fragmentManager.commit {
                    replace(containerId,fragment,"")
                    addToBackStack(fragment.tag)
                }
            }
        }
    }
}

infix fun FragmentLaunchInfo.launchIn(fragment: Fragment) {
    launchIn(fragment, fragment.getFragmentContainerId())
}

fun FragmentLaunchInfo.launchIn(
    fragment: Fragment,
    containerId: Int = fragment.getFragmentContainerId()
) {
    launchIn(
        fragment.parentFragmentManager,
        fragment.requireActivity(),
        containerId
    )
}

infix fun FragmentLaunchInfo.launchIn(activity: FragmentActivity) {
    launchIn(activity, Window.ID_ANDROID_CONTENT)
}

fun FragmentLaunchInfo.launchIn(
    activity: FragmentActivity,
    containerId: Int = Window.ID_ANDROID_CONTENT
) {
    launchIn(
        activity.supportFragmentManager,
        activity,
        containerId
    )
}

infix fun Fragment.withMode(mode: LaunchMode): FragmentLaunchInfo {
    return FragmentLaunchInfo(this, mode)
}

fun test(fragment: Fragment, parent: Fragment) {
    (fragment withMode LaunchMode.Standard) launchIn parent
}