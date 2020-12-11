package com.x930073498.module1

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.annotations.FragmentAnnotation
import com.x930073498.common.auto.IAuto

@FragmentAnnotation(path = "/module1/test")
class TestFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
class TestAuto:IAuto{

}