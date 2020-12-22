package com.x930073498.component.fragmentation

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.x930073498.component.auto.IAuto
import com.x930073498.component.core.IFragmentLifecycle

interface IFragmentation {
    fun onBackPressedSupport(): Boolean {
        return false
    }
}

class FragmentationAuto : IAuto, IFragmentLifecycle {
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        if (f is IFragmentation) {
            f.requireActivity().onBackPressedDispatcher.let {
                it.addCallback(f, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!f.onBackPressedSupport()) {
                            isEnabled = false
                            it.onBackPressed()
                        }
                    }
                })
            }
        }

    }

}