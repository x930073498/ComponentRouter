package com.x930073498.kotlinpoet.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.kotlinpoet.R
import com.x930073498.router.Router
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
@ActivityAnnotation(path = "/test/second")
class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        GlobalScope.launch {
            Router.from("test3?a=4&b=3").navigate<TestService>()?.test()
            println( Router.from("test4?a=4&b=3").bundle {
                putCharSequence("c","4")
            }.navigate<Any>())

        }

    }
}