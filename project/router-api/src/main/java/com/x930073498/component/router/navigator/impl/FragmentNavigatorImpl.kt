package com.x930073498.component.router.navigator.impl

import androidx.fragment.app.Fragment
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.coroutines.cast
import com.x930073498.component.router.coroutines.map
import com.x930073498.component.router.navigator.FragmentNavigator
import com.x930073498.component.router.navigator.FragmentNavigatorParams
import com.x930073498.component.router.navigator.NavigatorOption
import com.x930073498.component.router.navigator.NavigatorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal class FragmentNavigatorImpl(
    private val listenable: ResultListenable<FragmentNavigatorParams>,
    private val fragmentNavigatorOption: NavigatorOption.FragmentNavigatorOption,
) : FragmentNavigator {
    private var fragmentRef = WeakReference<Fragment>(null)
    override fun getFragment(): ResultListenable<Fragment> {
        val fragment = fragmentRef.get()
        if (fragment != null) return listenable.map { fragment }
        return listenable.map {
            it.run {
                target.action.run {
                    withContext(Dispatchers.Main.immediate) {
                        factory().create(contextHolder, target.targetClazz, bundle)
                    }.apply {
                        fragmentRef = WeakReference(this)
                    }
                }
            }
        }
    }

    override fun <T : Fragment> getInstanceFragment(clazz: Class<T>): ResultListenable<T> {
        return getFragment().cast()
    }


    override fun navigate(
    ): ResultListenable<NavigatorResult> {
        return getFragment().map {
            NavigatorResult.FRAGMENT(it)
        }
    }

}


inline fun <reified T> FragmentNavigator.getInstanceFragment(): ResultListenable<T> where  T : Fragment {
    return getInstanceFragment(T::class.java)
}

suspend inline fun <reified T> FragmentNavigator.instanceFragment(): T where T : Fragment {
    return instanceFragment(T::class.java)
}