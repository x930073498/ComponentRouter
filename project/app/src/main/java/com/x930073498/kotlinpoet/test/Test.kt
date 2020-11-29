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


@MethodAnnotation(path = "/test/test4")
suspend fun testMethod(
    @MethodBundleNameAnnotation("context") context: Context,
    @MethodBundleNameAnnotation("a") a: String,
    @MethodBundleNameAnnotation("b") b: Int,
    @MethodBundleNameAnnotation("c") c: CharSequence,
): String {
    return a + b + c
}


class TestMethodInvoker : MethodInvoker<String> {
    override suspend fun invoke(contextHolder: ContextHolder, bundle: Bundle): String? {
        val context = contextHolder.getContext()
        val a = ParameterSupport.getString(bundle, "a")
        val b = ParameterSupport.getInt(bundle, "b")
        val c = ParameterSupport.getCharSequence(bundle, "c")
        if (a == null || b == null || c == null) {
            return null
        }
        return testMethod(context, a, b, c)

    }

}

class TestMethodActionDelegate : MethodActionDelegate<TestMethodInvoker, String>,IAuto,AutoAction<String>() {
    override val path: String
        get() = "/test/test4"
    override val group: String
        get() = "test"
    override fun factory() = object :MethodActionDelegate.Factory<TestMethodInvoker> {
        override suspend fun create(
            contextHolder: ContextHolder,
            clazz: Class<TestMethodInvoker>,
            bundle: Bundle,
        ): TestMethodInvoker {
           return TestMethodInvoker()
        }

    }

    override suspend fun target() =
        Target.MethodTarget(String::class.java, TestMethodInvoker::class.java)

}


