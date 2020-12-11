package com.x930073498.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.common.auto.IAuto

@ActivityAnnotation(path = "/activity/main")
class MainActivity:AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}
class TestAuto:IAuto{
    init {

        println("enter this line 1111")
    }
}
