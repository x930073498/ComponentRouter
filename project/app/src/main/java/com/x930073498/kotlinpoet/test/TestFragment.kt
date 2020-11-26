package com.x930073498.kotlinpoet.test

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.x930073498.annotations.FactoryAnnotation
import com.x930073498.annotations.FragmentAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.kotlinpoet.R
import com.x930073498.router.*
import com.x930073498.router.action.ContextHolder
import com.x930073498.router.impl.FragmentActionDelegate
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

@Parcelize
data class A(val name: String) : Serializable, Parcelable

@FragmentAnnotation(path = "/test/a")
class TestFragment : Fragment(R.layout.fragment_test) {

    @ValueAutowiredAnnotation
    var name = ""

    @ValueAutowiredAnnotation("aaa")
    var a: A? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tv)?.text = name
        requireView().setOnClickListener {
            GlobalScope.launch {
                Router.from("/test/service?testA=8484848&b=4&c=5").navigate<TestService>()
            }
        }
    }

    @FactoryAnnotation
    class Factory : FragmentActionDelegate.Factory<TestFragment> {
        override suspend fun create(
            contextHolder: ContextHolder,
            clazz: Class<TestFragment>,
            bundle: Bundle,
        ): TestFragment {
            return TestFragment().also {
                it.arguments = bundle
            }
        }
    }

    companion object {

        @FactoryAnnotation
        fun create(bundle: Bundle): TestFragment {
            return TestFragment().also { it.arguments = bundle }
        }
    }
}



