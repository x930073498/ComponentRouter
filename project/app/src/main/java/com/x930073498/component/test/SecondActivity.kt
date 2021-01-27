package com.x930073498.component.test

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.x930073498.component.annotations.*
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.requireLifecycleOwner
import com.x930073498.component.databinding.ActivitySecondBinding
import com.x930073498.component.router.Router
import com.x930073498.component.router.coroutines.*
import com.x930073498.component.router.asActivity
import com.x930073498.component.router.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ActivityAnnotation(path = "/activity/second", interceptors = ["/test/interceptors/test1"])
//@FragmentAnnotation(path = "/activity/second", interceptors = ["/test/interceptors/test1"])
//@InterceptorAnnotation(path = "/activity/second")
//@ServiceAnnotation(path = "/activity/second")
//@MethodAnnotation(path = "/activity/second")
class SecondActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch {
            scopeResultOf {
                delay(2000)
                "1"
            }
                .bindLifecycle(requireLifecycleOwner())
                .map { it.toInt() }
                .await().apply {
                    LogUtil.log(this)
                }
        }
        binding.tvSecond.setOnClickListener {
            Router.from("/activity/navigation")
//                .asActivity(lifecycleScope,navigatorOption = NavigatorOption.ActivityNavigatorOption(launchMode = LaunchMode.SingleTop))
                .asActivity()
                .requestActivity()
                .forceEnd {
                    LogUtil.log(it)
                    setResult(RESULT_OK, Intent().putExtra("result", "result"))
                    toast("返回结果")
                    finish()
                }
                .bindLifecycle(this)

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
        Router.from("/method/toast") {
            uri {
                appendQueryParameter("info", "{msg:\"$msg\",duration:$duration}")
            }

        }
            .navigate()
//            .bindLifecycle(this)
            .listen {
                while (true) {
                    LogUtil.log("enter this line 989777777777")
                    delay(1000)
                }
            }
            .forceEnd()
    }
}