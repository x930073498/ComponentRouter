package com.x930073498.sample

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.common.auto.IAuto
import com.x930073498.router.Router
import kotlin.concurrent.thread

@ActivityAnnotation(path = "/activity/main")
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        thread {
            val fragment = Router.from("/fragment/test?name=测试").syncNavigation<Fragment>(this)
            if (fragment != null) {
                supportFragmentManager.beginTransaction().add(Window.ID_ANDROID_CONTENT, fragment)
                    .commit()
            }
        }
    }
}

class TestAuto : IAuto {
    init {
        println("enter this line 1111")
    }
}

@MethodAnnotation(path = "/method/toast")
suspend fun toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}