package com.x930073498.component.test

import com.x930073498.annotations.ServiceAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.router.impl.IService

interface TestService : IService {
    fun test()
}

@ServiceAnnotation(path = "/test/service")
class TestServiceImpl : TestService {
    @ValueAutowiredAnnotation("testA")
    var a:String?=""

    override suspend fun invoke() {
        println(a)
    }
    override fun test() {
        println("enter this line TestService")
    }
}