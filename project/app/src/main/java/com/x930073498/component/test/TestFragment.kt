package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.R
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.fragmentation.IFragmentation
import com.x930073498.component.fragmentation.startWithRouter
import com.x930073498.component.router.Router
import com.x930073498.component.router.action.PathState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@FragmentAnnotation(path = "/test/a", autoRegister = true)
open class TestFragment : TestParentFragment(), IFragmentation {
    @ValueAutowiredAnnotation
    var title = ""
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tvTitle)?.apply {
            text = title
            setOnClickListener {
                if (Router.loadRealPath("/module1/test")) {
                    toast("路径加载成功")
                }
            }
        }
        view.findViewById<View>(R.id.tv).setOnClickListener {
//            Router.from("yangpijun://yunzhanxinxi.com/taoke/module/main/life/fragment/promote?platform=elm").forwardSync(requireContext())
//            Router.from("http://www.baidu.com").forwardSync(
//                requireContext(),
//                WebFragmentNavigateInterceptor,
//                StartFragmentResultHandler
//            )
//                startWithRouter("http://www.baidu.com") {
//                    this.withNavOptions {
//                        launchSingleTop = true
//                    }
//                }
            when (Router.ofHandle().getRealPathState("/module1/test")) {
                PathState.NONE -> {
                    toast("路径尚未注册")
                }

                is PathState.LOADED -> {
                    startWithRouter("/module1/test?name=模块测试1") {
                        this.withNavOptions {
                            launchSingleTop = true
                        }
                    }
                }
                is PathState.UNLOADED -> {
                    toast("路径尚未加载")
                }
            }

//                startWithRouter("yangpijun://yunzhanxinxi.com/taoke/module/main/life/fragment/promote?platform=elm"){
//
//                }
        }

    }


}

fun toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Router.from("/method/toast")
        .serializer {
            put("info", mapOf("msg" to msg, "duration" to "$duration"))
        }
        .navigate()
}



