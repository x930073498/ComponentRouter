package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.R
import com.x930073498.component.StartFragmentResultHandler
import com.x930073498.component.WebFragmentNavigateInterceptor
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.fragmentation.IFragmentation
import com.x930073498.component.fragmentation.startWithRouter
import com.x930073498.component.router.Router
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@FragmentAnnotation(path = "/test/a")
open class TestFragment : TestParentFragment(), IFragmentation {
    @ValueAutowiredAnnotation
    var title = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.log("enter this line 987426")
        requireView().findViewById<TextView>(R.id.tvTitle)?.text = title
        view.setOnClickListener {
//            Router.from("yangpijun://yunzhanxinxi.com/taoke/module/main/life/fragment/promote?platform=elm").forwardSync(requireContext())
//            Router.from("http://www.baidu.com").forwardSync(
//                requireContext(),
//                WebFragmentNavigateInterceptor,
//                StartFragmentResultHandler
//            )
            GlobalScope.launch {
//                startWithRouter("http://www.baidu.com") {
//                    this.withNavOptions {
//                        launchSingleTop = true
//                    }
//                }
                startWithRouter("/module1/test?name=模块测试1") {
                    this.withNavOptions {
                        launchSingleTop = true
                    }
                }
//                startWithRouter("yangpijun://yunzhanxinxi.com/taoke/module/main/life/fragment/promote?platform=elm"){
//
//                }
            }

        }
    }

    override fun onBackPressedSupport(): Boolean {
        LogUtil.log("enter this line onBackPressedSupport")
        return true
    }

}



