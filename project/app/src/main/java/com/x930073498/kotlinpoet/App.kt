package com.x930073498.kotlinpoet

import android.app.Application
import com.x930073498.kotlinpoet.test.TestModuleRegister
import com.x930073498.router.Router
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var foo:Foo
    override fun onCreate() {
        super.onCreate()
        foo.test()

    }
    fun a(){
        println("enter this line App ")
    }
}