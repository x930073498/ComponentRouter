package com.x930073498.component.test

import android.os.Bundle
import com.x930073498.component.annotations.ServiceAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.impl.IService

interface TestService : IService {
    fun test()
}

interface TestService1 : TestService


@ServiceAnnotation(
    path = "/test/service/parent",
    singleton = true,
    autoInvoke = true,
)
open class TestParentServiceImpl : TestService {
    init {
        LogUtil.log("enter this line init TestParentServiceImpl")
    }

    @ValueAutowiredAnnotation("testA")
    var a: String? = ""

    override suspend fun invoke() {
        LogUtil.log(a)
    }

    override fun test() {
        LogUtil.log("enter this line TestService")
    }
}

@ServiceAnnotation(
    path = "/test/service/1",
    interceptors = ["/test/interceptors/test2",   "/test/interceptors/test1"],
    singleton = true,
    autoInvoke = true,
)
class TestParentService1 : TestService1 {
    init {
        LogUtil.log("enter this line init TestParentService1")
    }

    @ValueAutowiredAnnotation("testA")
    var a: String? = ""

    override suspend fun invoke() {
        LogUtil.log(a)
    }

    override fun test() {
        LogUtil.log("enter this line TestService1")
    }
}

@ServiceAnnotation(
    path = "/test/service",
    singleton = true,
    autoInvoke = true,
)
class TestServiceImpl : TestParentServiceImpl() {
    override fun init(contextHolder: ContextHolder, bundle: Bundle) {
        super.init(contextHolder, bundle)

    }

    init {
        LogUtil.log("enter this line init TestServiceImpl")
    }
}