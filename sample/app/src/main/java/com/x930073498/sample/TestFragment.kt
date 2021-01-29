package com.x930073498.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.fragmentation.popSelf
import com.x930073498.component.fragmentation.startWithRouter
import com.x930073498.component.router.Router
import com.x930073498.component.router.asMethod
import com.x930073498.component.router.coroutines.bindLifecycle
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.sample.databinding.AppFragmentTestBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@FragmentAnnotation(path = "/app/fragment/test")
class TestFragment : Fragment(R.layout.app_fragment_test) {
    private val binding: AppFragmentTestBinding
        get() {
            return AppFragmentTestBinding.bind(requireView())
        }

    @ValueAutowiredAnnotation("name")
    var title: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv.text = title

        binding.tv.setOnClickListener {
            Router.from("/method/toast?msg=测试").asMethod().getMethodInvoker().listen {
                it.invoke()
            }
            startWithRouter("/app/activity/second") {
                withNavOptions {
                    popUpTo(Router.ofHandle().getRealPathFromTarget(this@TestFragment)!!) {
                        this.inclusive = true
                    }
                }
            }
                .bindLifecycle(this)
        }
    }


}