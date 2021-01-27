package com.x930073498.component.router.core

import android.content.Context
import com.x930073498.component.router.coroutines.AwaitResultCoroutineScope
import com.x930073498.component.router.coroutines.ResultListenable
import com.x930073498.component.router.response.RouterResponse
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

abstract class IRequestRouter{
   internal abstract suspend fun requestInternal(
       coroutineContext: CoroutineContext? = null,
       debounce: Long = 600L,
       context: Context? = null
    ): ResultListenable<RouterResponse>

    internal abstract fun requestInternal(
        scope: CoroutineScope = AwaitResultCoroutineScope,
        coroutineContext: CoroutineContext = scope.coroutineContext,
        debounce: Long = 600L,
        context: Context? = null
    ): ResultListenable<RouterResponse>


}