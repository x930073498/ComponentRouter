package com.x930073498.kotlinpoet

import android.app.Activity
import android.os.Bundle
import com.zx.common.auto.IActivityLifecycle
import com.zx.common.auto.IAuto

class TestAutoTask : IAuto,IActivityLifecycle {


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        println("enter this line 1401410")
    }
}