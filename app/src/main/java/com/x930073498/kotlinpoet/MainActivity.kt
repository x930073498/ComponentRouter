package com.x930073498.kotlinpoet

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.router.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ActivityAnnotation(path = "/test")
class MainActivity : AppCompatActivity() {
    @ValueAutowiredAnnotation("name")
    var name:String=""
    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val uri = Uri.parse("test/a?name=24254")
        val router = Router.from(uri)
        GlobalScope.launch(Dispatchers.Main) {
            val fragment = router.navigate<Fragment>()
            if (fragment != null) {
                supportFragmentManager.beginTransaction().add(R.id.container, fragment)
                    .commitAllowingStateLoss()
            }
        }
//

    }


}