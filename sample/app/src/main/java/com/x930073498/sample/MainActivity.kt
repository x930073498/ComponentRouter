package com.x930073498.sample

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.MethodAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.auto.deserialize
import com.x930073498.component.auto.getSerializer
import com.x930073498.component.fragmentation.loadRootFromRouter

@ActivityAnnotation(path = "/activity/main")
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        loadRootFromRouter(
            Window.ID_ANDROID_CONTENT,
            "/module1/fragment/test?name=测试",
            scope = lifecycleScope
        ) {
            bundle("name", "AA")
        }
    }
}


@MainThread
@MethodAnnotation(path = "/method/toast")
fun toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

