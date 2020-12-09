package com.x930073498.component.test

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.x930073498.annotations.FragmentAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.component.R


@FragmentAnnotation(path = "/test/a")
class TestFragment : TestParentFragment() {
    @ValueAutowiredAnnotation
    var title = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.tvTitle)?.text = title
    }

}



