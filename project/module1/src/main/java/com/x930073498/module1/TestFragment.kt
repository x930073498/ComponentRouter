package com.x930073498.module1

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.MethodAnnotation
import com.x930073498.component.auto.IAuto
import com.x930073498.component.core.LogUtil
import com.x930073498.component.router.Router

@FragmentAnnotation(path = "/module1/test")
class TestFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
class TestAuto: IAuto {

}
@MethodAnnotation(path = "/module1/method/test")
suspend fun doTest(context: Context){
    LogUtil.log("enter this line 7878744")
    Router.from("/method/toast").bundle {
        putString("msg","测试")
    }.forward(context)
}
