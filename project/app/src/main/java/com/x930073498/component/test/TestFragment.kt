package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.R
import com.x930073498.component.StartFragmentResultHandler
import com.x930073498.component.WebFragmentNavigateInterceptor
import com.x930073498.component.router.Router


@FragmentAnnotation(path = "/test/a")
open class TestFragment : TestParentFragment() {
    @ValueAutowiredAnnotation
    var title = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tvTitle)?.text = title
        view.setOnClickListener {
//            Router.from("yangpijun://yunzhanxinxi.com/taoke/module/main/life/fragment/promote?platform=elm").forwardSync(requireContext())
            Router.from("http://www.baidu.com").forwardSync(
                requireContext(),
                WebFragmentNavigateInterceptor,
                StartFragmentResultHandler
            )
        }
    }

}



