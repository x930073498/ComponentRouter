package com.x930073498.kotlinpoet.test

import android.content.Context
import android.os.Bundle
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.annotations.MethodBundleNameAnnotation
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.MethodActionDelegate
import com.x930073498.router.impl.MethodInvoker
import com.x930073498.router.action.MethodTarget
import com.x930073498.router.util.ParameterSupport


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
        println("enter method testMethod bundle=$bundle")
        val context = contextHolder.getContext()
        val a = ParameterSupport.getString(bundle, "a")
        println("a=$a")
        val b = ParameterSupport.getInt(bundle, "b")
        println("b=$b")
        val c = ParameterSupport.getCharSequence(bundle, "c")
        println("c=$c")
        if (a == null || b == null || c == null) {
            return null
        }
        return testMethod(context, a, b, c)

    }

}

class TestMethodActionDelegate : MethodActionDelegate<TestMethodInvoker, String> {
    override val path: String
        get() = "test4"

    override fun factory() = object :MethodActionDelegate.Factory<TestMethodInvoker> {
        override suspend fun create(
            contextHolder: ContextHolder,
            clazz: Class<TestMethodInvoker>,
            bundle: Bundle,
        ): TestMethodInvoker? {
           return TestMethodInvoker()
        }

    }

    override suspend fun target() = MethodTarget(String::class.java, TestMethodInvoker::class.java)

}


