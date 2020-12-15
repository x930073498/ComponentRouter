package com.x930073498.component

import android.net.Uri
import androidx.core.os.bundleOf
import com.x930073498.component.router.action.*
import com.x930073498.component.router.impl.navigate
import com.x930073498.component.router.util.ParameterSupport

object WebFragmentNavigateInterceptor : NavigateInterceptor {
    override suspend fun intercept(chain: NavigateChain): NavigateResult {
        val params = chain.request()
        val url = ParameterSupport.getUri(params.bundle) ?: return chain.process(params)
        val scheme = url.scheme
        if (scheme == "http" || scheme == "https") {
            val bundle = bundleOf()
            bundle.putAll(params.bundle)
            bundle.putString("url", url.toString())
            val uri = Uri.parse("/fragment/web")
            ParameterSupport.syncUriToBundle(uri, bundle)
            return chain.process(NavigateParams(bundle, params.contextHolder))
        }
        return chain.process(params)
    }
}