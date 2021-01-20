package com.x930073498.component.test

import com.x930073498.component.annotations.ServiceAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.impl.IService

interface TestService : IService {
    fun test()
}

@ServiceAnnotation(
    path = "/test/service",
    singleton = true,
    autoInvoke = true,
)
class TestServiceImpl : TestService {
    init {
        LogUtil.log("enter this line init TestServiceImpl")
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