package com.x930073498.component.router.navigator

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.impl.IService

interface ParameterProvider {
    fun getBundle(): Bundle
    fun getContextHolder(): ContextHolder
}

interface Navigator {
    suspend fun forward() {
        navigate()
    }

    suspend fun navigate(): Any?
}


suspend inline fun <reified T : Activity> ActivityNavigator.requestInstanceActivity(): T {
    return requestInstanceActivity(T::class.java)
}

suspend inline fun <reified T : Fragment> FragmentNavigator.getInstanceFragment(): T {
    return getInstanceFragment(T::class.java)
}

suspend inline fun <reified T : IService> ServiceNavigator.getInstanceService(): T {
    return getInstanceService(T::class.java)
}



