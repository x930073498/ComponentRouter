package com.x930073498.component

import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.x930073498.component.router.action.NavigateParams
import com.x930073498.component.router.impl.ResultHandler

object StartFragmentResultHandler : ResultHandler {
    override suspend fun handle(result: Any?, params: NavigateParams): Any? {
        if (result is Fragment) {
            val context = params.contextHolder.getContext()
            if (context is FragmentActivity) {

                context.supportFragmentManager.beginTransaction()
                    .replace(Window.ID_ANDROID_CONTENT, result,"a")
                    .addToBackStack("a")
                    .commit()
                return null
            }
        }
        return result
    }
}