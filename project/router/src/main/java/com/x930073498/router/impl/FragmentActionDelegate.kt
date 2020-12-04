package com.x930073498.router.impl

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target


interface FragmentActionDelegate: ActionDelegate{

    fun inject(bundle: Bundle, target: Fragment)

    suspend fun factory(): Factory

    suspend fun target(): Target.FragmentTarget

    interface Factory {
        suspend fun create(contextHolder: ContextHolder, clazz: Class<*>, bundle: Bundle):  Fragment
    }

}
