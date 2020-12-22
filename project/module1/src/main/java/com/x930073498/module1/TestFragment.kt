package com.x930073498.module1

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.MethodAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.fragmentation.startWithRouter
import com.x930073498.component.router.Router
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.module1.databinding.FragmentModuleTestBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder



@FragmentAnnotation(path = "/module1/test")
class TestFragment : Fragment(R.layout.fragment_module_test) {
    @ValueAutowiredAnnotation(name = "name")
    var name: String = ""
    private val binding: FragmentModuleTestBinding
        get() = FragmentModuleTestBinding.bind(requireView())


    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = ParameterSupport.get(arguments, "name") ?: ""
        arguments?.keySet()?.forEach {
            LogUtil.log("enter this line argument key=$it,value=${arguments?.get(it)}")
        }
        LogUtil.log("enter this line onViewCreated title=$name")
        binding.tv.text = name
        binding.tv.setOnClickListener {
            GlobalScope.launch {
                startWithRouter("/module1/test?name=模块测试2") {
//                    this.withNavOptions {
//                        launchSingleTop = true
//                    }
                }
            }
        }
    }
}

@MethodAnnotation(path = "/module1/method/test")
suspend fun doTest(context: Context) {
    LogUtil.log("enter this line 7878744")
    Router.from("/method/toast?info=${URLEncoder.encode("{msg:\"测试\"}")}").bundle {
//        put("msg","测试")

    }.uri {
//        appendQueryParameter("info", getSerializer().serialize(ToastInfo("msg",0).also {
//            LogUtil.log(it)
//        }))
//        appendQueryParameter("info", "{msg:\"测试\",duration:0}")
    }
        .forward(context)
}

