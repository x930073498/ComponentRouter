package com.x930073498.component.router.navigator

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import com.x930073498.component.router.action.*
import com.x930073498.component.router.action.Target
import com.x930073498.component.router.impl.ResultHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

interface FragmentNavigator : ParameterProvider, Navigator {
    companion object {
        internal fun create(
            target: Target.FragmentTarget,
            contextHolder: ContextHolder,
            bundle: Bundle
        ): FragmentNavigator {
            return object : FragmentNavigator {
                private var fragmentRef = WeakReference<Fragment>(null)
                override suspend fun getFragment(): Fragment {
                    val fragment = fragmentRef.get()
                    if (fragment != null) return fragment
                    return target.action.run {
                        if (Looper.getMainLooper() == Looper.getMainLooper()) {
                            factory().create(contextHolder, target.targetClazz, bundle)
                        } else {
                            withContext(Dispatchers.Main.immediate) {
                                factory().create(contextHolder, target.targetClazz, bundle)
                            }
                        }
                    }.apply {
                        fragmentRef = WeakReference(this)
                    }
                }

                override suspend fun <T : Fragment> getInstanceFragment(clazz: Class<T>): T {
                    return runCatching {
                        getFragment() as T
                    }.getOrElse {
                        throw RuntimeException("目标 ${fragmentRef.get()} 不能强转成$clazz")
                    }
                }

                override fun getBundle(): Bundle {
                    return bundle
                }

                override fun getContextHolder(): ContextHolder {
                    return contextHolder
                }

                override suspend fun navigate(
                ): Any? {
                    return getFragment()
                }

            }
        }
    }

    suspend fun getFragment(): Fragment

    suspend fun <T : Fragment> getInstanceFragment(clazz: Class<T>): T

}