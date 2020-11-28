package com.x930073498.router.impl

import com.x930073498.router.action.ActionCenter

@Suppress("LeakingThis")
abstract class AutoAction<T>:ActionDelegate<T> {
//    init {
//        ActionCenter.register(getActionDelegate())
//    }
//
//   private fun getActionDelegate():ActionDelegate<T>{
//        return this
//    }
}