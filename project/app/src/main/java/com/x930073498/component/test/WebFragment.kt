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
import com.x930073498.component.fragmentation.IFragmentation

@FragmentAnnotation(path = "/fragment/web",desc = "网页")
class WebFragment : Fragment(R.layout.fragment_web), IFragmentation {

    @ValueAutowiredAnnotation("title")
    var mTitle: String = ""

    @ValueAutowiredAnnotation("url")
    var url: String = ""

    private val binding by lazy {
        FragmentWebBinding.bind(requireView())
    }
    private var agentWeb: AgentWeb? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = mTitle
        agentWeb = AgentWeb.with(this).setAgentWebParent(
            binding.webView, -1, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
            .useDefaultIndicator(R.color.colorPrimary)
            .createAgentWeb()
            .ready()
            .go(url)
    }

    override fun onBackPressedSupport(): Boolean {
        val temp = agentWeb ?: return false
        val web = temp.webCreator.webView
        if (web.canGoBack()) {
            web.goBack()
            return true
        }
        return false
    }
}