package com.x930073498.component.router.core

import android.content.Context
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.action.ActionCenter
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.util.ParameterSupport
import java.lang.RuntimeException

internal class ClassRequestRouterImpl<T>(clazz: Class<T>) : IClassRequestRouter<T>(clazz) {
    private val mHandler = InternalRouterHandler()
    override fun requestInternalWithClass(
        context: Context?,
        request: IRouterHandler.() -> Unit
    ): T? {

        request(mHandler)
        val bundle = mHandler.mBundle
        val uri = mHandler.uriBuilder.build()
        ParameterSupport.syncUriToBundle(uri, bundle)
        val contextHolder = ContextHolder.create(context)
        return ActionCenter.getTarget(clazz, bundle, contextHolder)
    }
}