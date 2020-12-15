package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FactoryAnnotation
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.R
import com.x930073498.component.core.LogUtil
import com.x930073498.component.router.*
import com.x930073498.component.router.action.ContextHolder
import com.x930073498.component.router.impl.FragmentActionDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@FragmentAnnotation(path = "/test/parent")
open class TestParentFragment : Fragment(R.layout.fragment_test) {
    @ValueAutowiredAnnotation
    var name = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tv)?.text = name
        requireView().setOnClickListener {
//            Router.from("http://www.baidu.com").syncNavigation<Any>()
            Router.getServiceSync<TestService>()?.test()
            GlobalScope.launch {
                LogUtil.log("enter this line 987456")
//                Router.from("/a/test/test4?a=method&b=14&c=test").navigate<String>(requireContext())?.also {
//                    println(it)
//                }

//                Router.from("/test/service?testA=8484848&b=4&c=5").navigate<TestService>()
            }
        }
    }

    @FactoryAnnotation
    class Factory : FragmentActionDelegate.Factory {
        override  suspend fun create(
            contextHolder: ContextHolder,
            clazz: Class<*>,
            bundle: Bundle,
        ): TestParentFragment {
            return TestParentFragment().also {
                it.arguments = bundle
            }
        }
    }

    companion object {

        @FactoryAnnotation
        fun create(bundle: Bundle): TestParentFragment {
            return TestParentFragment().also { it.arguments = bundle }
        }
    }
}



