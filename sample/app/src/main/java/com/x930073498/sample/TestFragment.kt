package com.x930073498.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.router.Router
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
        binding.tv.setOnClickListener {
            Router.from("/method/toast?msg=测试").forwardSync(requireContext())
        }
    }


}