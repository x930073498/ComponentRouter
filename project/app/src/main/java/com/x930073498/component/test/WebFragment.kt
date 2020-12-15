package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.just.agentweb.AgentWeb
import com.x930073498.component.R
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.databinding.FragmentWebBinding

@FragmentAnnotation(path = "/fragment/web")
class WebFragment : Fragment(R.layout.fragment_web) {

    @ValueAutowiredAnnotation("title")
    var title: String = ""

    @ValueAutowiredAnnotation("url")
    var url: String = ""

    private val binding by lazy {
        FragmentWebBinding.bind(requireView())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = title
        AgentWeb.with(this).setAgentWebParent(
            binding.webView, -1, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
            .useDefaultIndicator(R.color.colorPrimary)
            .createAgentWeb()
            .ready()
            .go(url)
    }
}