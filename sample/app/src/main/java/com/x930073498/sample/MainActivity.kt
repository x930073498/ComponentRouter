package com.x930073498.sample

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.MethodAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.auto.deserialize
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.fragmentation.loadRootFromRouter
import com.x930073498.component.router.Router
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ActivityAnnotation(path = "/activity/main")
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        LogUtil.log(Data("测试"))
        val s = S().apply { www("测试") }
        val sString = getSerializer().serialize(s)
        println("sString=$sString")
        val a = getSerializer().deserialize<W>(sString)
        println("a=${a?.uuu()}")
        GlobalScope.launch {
            loadRootFromRouter(Window.ID_ANDROID_CONTENT, "/module1/fragment/test?name=测试") {
                put("name", "AA")
            }
        }
    }
}


@MainThread
@MethodAnnotation(path = "/method/toast")
fun toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

