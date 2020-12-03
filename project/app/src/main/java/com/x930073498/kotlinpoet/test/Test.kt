package com.x930073498.kotlinpoet.test

import android.content.Context
import android.os.Bundle
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.annotations.MethodBundleNameAnnotation
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.action.Target
import com.x930073498.router.impl.AutoAction
import com.x930073498.router.impl.MethodActionDelegate
import com.x930073498.router.impl.MethodInvoker
import com.x930073498.router.util.ParameterSupport
import com.zx.common.auto.IAuto
import kotlinx.coroutines.delay


@MethodAnnotation(path = "/test/test4")
 suspend fun testMethod(
    @MethodBundleNameAnnotation("context") context: Context,
    @MethodBundleNameAnnotation("a") a: String?,
    @MethodBundleNameAnnotation("b") b: Int?,
    @MethodBundleNameAnnotation("c") c: CharSequence,
) {
    delay(1200)
    println("enter this line 897 $context")
     a + b + c
}


