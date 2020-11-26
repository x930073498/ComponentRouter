package com.x930073498.kotlinpoet

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class ActivityFoo @Inject constructor(@ActivityContext val context: Context) {
    fun test(){
        println("enter this line Foo context=$context")
    }
}