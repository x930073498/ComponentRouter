package com.x930073498.kotlinpoet.test

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
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


@FragmentAnnotation(path = "/test/a")
class TestFragment : TestParentFragment() {
    @ValueAutowiredAnnotation
    var title = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tvTitle)?.text = title
    }

}



