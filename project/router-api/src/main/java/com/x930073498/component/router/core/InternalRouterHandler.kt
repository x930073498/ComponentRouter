package com.x930073498.component.router.core

import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.core.os.bundleOf
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.router.util.ParameterSupport
import java.io.Serializable

internal fun createFormBundle(bundle: Bundle): ISerializerBundle {
    return object : ISerializerBundle {
        override fun put(key: String, value: Any?) {
            bundle.putBoolean(ParameterSupport.getSerializerKey(key), true)
            bundle.putString(
                key,
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

internal class InternalRouterHandler(uri: Uri = Uri.EMPTY, bundle: Bundle = bundleOf()) :
    IRouterHandler {
    internal var uriBuilder = uri.buildUpon()
    internal val mBundle = bundle
    internal val interceptors = arrayListOf<String>()
    internal val replaceInterceptors = arrayListOf<String>()

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

    override fun serializer(key: String, value: Any?): IRouterHandler {
        iBundle.put(key, value)
        return this
    }

    override fun bundle(action: Bundle.() -> Unit): IRouterHandler {
        action(mBundle)
        return this
    }

    override fun bundle(key: String, value: Any?): IRouterHandler {
        when (value) {
            null -> return this
            is String -> mBundle.putString(key, value)

            is Boolean -> mBundle.putBoolean(key, value)
            is Int -> mBundle.putInt(key, value)
            is Float -> mBundle.putFloat(key, value)
            is Double -> mBundle.putDouble(key, value)
            is Byte -> mBundle.putByte(key, value)
            is Long -> mBundle.putLong(key, value)
            is Short -> mBundle.putShort(key, value)
            is Char -> mBundle.putChar(key, value)

            is ByteArray -> mBundle.putByteArray(key, value)
            is BooleanArray -> mBundle.putBooleanArray(key, value)
            is IntArray -> mBundle.putIntArray(key, value)
            is FloatArray -> mBundle.putFloatArray(key, value)
            is DoubleArray -> mBundle.putDoubleArray(key, value)
            is LongArray -> mBundle.putLongArray(key, value)
            is CharArray -> mBundle.putCharArray(key, value)
            is ShortArray -> mBundle.putShortArray(key, value)


            is Array<*> -> {
                val componentType = value::class.java.componentType!!
                @Suppress("UNCHECKED_CAST") // Checked by reflection.
                when {
                    Parcelable::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putParcelableArray(key, value as Array<Parcelable>)
                    }
                    String::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putStringArray(key, value as Array<String>)
                    }
                    CharSequence::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putCharSequenceArray(key, value as Array<CharSequence>)
                    }
                    Serializable::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putSerializable(key, value)
                    }
                    else -> {
                        val valueType = componentType.canonicalName
                        throw IllegalArgumentException(
                            "Illegal value array type $valueType for key \"$key\""
                        )
                    }
                }
            }
            is Bundle -> mBundle.putBundle(key, value)
            is CharSequence -> mBundle.putCharSequence(key, value)
            is Parcelable -> mBundle.putParcelable(key, value)
            is ArrayList<*> -> {
                val componentType = value::class.java.typeParameters[0].genericDeclaration
                when {
                    Parcelable::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putParcelableArrayList(key, value as ArrayList<Parcelable>)
                    }
                    Int::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putIntegerArrayList(key, value as ArrayList<Int>)
                    }
                    String::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putStringArrayList(key, value as ArrayList<String>)
                    }
                    CharSequence::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putCharSequenceArrayList(key, value as ArrayList<CharSequence>)
                    }
                    Serializable::class.java.isAssignableFrom(componentType) -> {
                        mBundle.putSerializable(key, value)

                    }

                }
            }
            is Binder -> mBundle.putBinder(key, value)
            is Size -> mBundle.putSize(key, value)
            is SizeF -> mBundle.putSizeF(key, value)

            is SparseArray<*> -> {
                LogUtil.log("enter this line SparseArrayParcelable")
                mBundle.putSparseParcelableArray(key, value as SparseArray<out Parcelable>)
            }
            is Serializable -> mBundle.putSerializable(key, value)
        }


        return this
    }

    override fun interceptors(vararg path: String): IRouterHandler {
        replaceInterceptors.addAll(path.asList())
        return this
    }

    override fun addInterceptor(vararg path: String): IRouterHandler {
        if (interceptors.isEmpty()) {
            if (path.isNotEmpty())
                interceptors.addAll(path)
            return this
        }
        interceptors.removeAll(path)
        interceptors.addAll(path)
        return this
    }


}