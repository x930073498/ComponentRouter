package com.x930073498.module1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.IAuto
import com.x930073498.component.router.Router
import com.x930073498.module1.databinding.FragmentTestBinding

class TestAuto: IAuto {
    init {
        println("enter this line 333")
    }
}
@FragmentAnnotation(path = "/fragment/test")
class TestFragment:Fragment(){
    @ValueAutowiredAnnotation(name = "name")
    var name:String?=""
    private val binding by lazy {
        FragmentTestBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv.text=name
        binding.tv.setOnClickListener {
            Router.from("/method/toast?msg=测试").forwardSync(requireContext())
        }
    }
}