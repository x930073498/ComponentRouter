package com.x930073498.component.router.impl

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.component.router.action.ContextHolder


interface FragmentActionDelegate: ActionDelegate{
    override fun type(): ActionType {
        return ActionType.FRAGMENT
    }



     fun factory(): Factory


    interface Factory {
         fun create(contextHolder: ContextHolder, clazz: Class<*>, bundle: Bundle):  Fragment
    }

}
