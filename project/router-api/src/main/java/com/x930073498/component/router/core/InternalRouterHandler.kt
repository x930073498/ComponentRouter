package com.x930073498.component.router.core

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.router.Router
import com.x930073498.component.router.util.ParameterSupport
import kotlin.properties.Delegates

internal fun createFormBundle(bundle: Bundle): ISerializerBundle {
    return object : ISerializerBundle {
        override fun put(key: String, value: Any?) {
            bundle.putString(
                ParameterSupport.getSerializerKey(key),
                when (value) {
                    null -> null
                    is String -> value
                    else -> getSerializer().serialize(value)
                }
            )
        }

        override fun clear() {
            bundle.clear()
        }

    }
}
internal class InternalRouterHandler(uri: Uri = Uri.EMPTY) :
    IRouterHandler {
    internal var uriBuilder = uri.buildUpon()
    internal val mBundle = bundleOf()

    internal var greenChannel = false
    private val iBundle = createFormBundle(mBundle)

    override fun greenChannel(): IRouterHandler {
        this.greenChannel = true
        return this
    }

    override fun scheme(scheme: String): IRouterHandler {
        uriBuilder.scheme(scheme)
        return this
    }

    override fun query(query: String): IRouterHandler {
        uriBuilder.query(query)
        return this
    }

    override fun path(path: String): IRouterHandler {
        uriBuilder.path(path)
        return this
    }

    override fun authority(authority: String): IRouterHandler {
        uriBuilder.authority(authority)
        return this
    }

    override fun appendQuery(key: String, value: String): IRouterHandler {
        uriBuilder.appendQueryParameter(key, value)
        return this
    }


    override fun uri(action: Uri.Builder.() -> Unit): IRouterHandler {
        action(uriBuilder)
        return this
    }

    override fun serializer(action: ISerializerBundle.() -> Unit): IRouterHandler {
        action(iBundle)
        return this
    }

    override fun bundle(action: Bundle.() -> Unit): IRouterHandler {
        action(mBundle)
        return this
    }

    override fun bundle(key: String, value: Any?): IRouterHandler {
        iBundle.put(key, value)
        return this
    }


}