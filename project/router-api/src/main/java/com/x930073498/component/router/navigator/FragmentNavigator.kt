package com.x930073498.component.router.navigator

import androidx.fragment.app.Fragment
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.navigator.impl.FragmentNavigatorImpl


interface FragmentNavigator : Navigator {
    companion object {
        internal fun create(
            listenable: ResultListenable<FragmentNavigatorParams>,
            navigatorOption: NavigatorOption,
        ): FragmentNavigator {
            val fragmentNavigatorOption =
                navigatorOption as? NavigatorOption.FragmentNavigatorOption
                    ?: NavigatorOption.FragmentNavigatorOption()
            return FragmentNavigatorImpl(listenable, fragmentNavigatorOption)
        }
    }

    fun getFragment(): ResultListenable<Fragment>

    suspend fun fragment(): Fragment {
        return getFragment().await()
    }

    suspend fun <T> instanceFragment(clazz: Class<T>): T where T : Fragment {
        return getInstanceFragment(clazz).await()
    }

    fun <T : Fragment> getInstanceFragment(clazz: Class<T>): ResultListenable<T>

}