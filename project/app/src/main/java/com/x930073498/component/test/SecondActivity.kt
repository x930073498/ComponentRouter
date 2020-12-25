package com.x930073498.component.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.R
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.databinding.ActivitySecondBinding
import com.x930073498.component.router.Router
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
@ActivityAnnotation(path = "/activity/second",interceptors = ["/test/interceptors/test1"])
class SecondActivity : AppCompatActivity() {
  private  val binding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tvSecond.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra("result","result"))
            finish()
        }
//        GlobalScope.launch {
//            Router.from("test3?a=4&b=3").navigate<TestService>()?.test()
//            LogUtil.log( Router.from("test4?a=4&b=3").bundle {
//                put("c","4")
//            }.navigate<Any>())
//
//        }


    }
}