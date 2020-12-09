package com.x930073498.component

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var foo:Foo
    override fun onCreate() {
        super.onCreate()
        foo.test()

    }
    fun a(){
        println("enter this line App")
    }
}