package com.x930073498.component.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.R
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.databinding.ActivitySecondBinding
import com.x930073498.component.router.Router
import com.x930073498.component.router.coroutines.bindLifecycle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@ActivityAnnotation(path = "/activity/second", interceptors = ["/test/interceptors/test1"])
class SecondActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tvSecond.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra("result", "result"))
            toast("返回结果")
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

    fun toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Router.from("/method/toast").uri {
            appendQueryParameter("info", "{msg:\"$msg\",duration:$duration}")
        }
            .serializer {

//                put(
//                    "info", JSONObject().put("msg", msg)
//                        .put("duration", duration).toString()
//                )
            }
            .navigate()
            .bindLifecycle(this)
            .listen {
                while (true){
                    LogUtil.log("enter this line 989777777777")
                    delay(1000)
                }
            }
    }
}