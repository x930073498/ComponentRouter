package com.x930073498.component

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Foo @Inject constructor(@ApplicationContext val context: Context) {


    fun test(){
        println("enter this line Foo context=$context")
    }
}