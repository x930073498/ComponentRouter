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
import com.x930073498.component.router.Router
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.sample.databinding.AppFragmentTestBinding

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
        registerForActivityResult(object : ActivityResultContract<Intent, String>() {
            override fun createIntent(context: Context, input: Intent): Intent {
                return input
            }

            override fun parseResult(resultCode: Int, intent: Intent?): String {
                return ParameterSupport.get(intent?.extras, "result", "") ?: ""
            }

        }, object : ActivityResultCallback<String> {
            override fun onActivityResult(result: String?) {
                Router.from("/method/toast?msg=$result").forwardSync(requireContext())
            }

        }).launch(Intent())
        binding.tv.setOnClickListener {
//            Router.from("/method/toast?msg=测试").forwardSync(requireContext())
            Router.from("/app/activity/second").forwardSync(requireContext())
        }
    }


}