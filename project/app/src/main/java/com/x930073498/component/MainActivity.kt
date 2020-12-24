package com.x930073498.component

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.*
import com.x930073498.component.router.impl.RouterInterceptor
import com.x930073498.component.router.interceptor.Chain
import com.x930073498.component.router.request.RouterRequest
import com.x930073498.component.router.response.RouterResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject


@ActivityAnnotation(path = "/test/test")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var app: Application

    @Inject
    lateinit var foo: ActivityFoo

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    @ValueAutowiredAnnotation("name")
    var name: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foo.test()
        viewModel.test()
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
        AgentWebConfig.debug()
        val uri = Uri.parse("/test/a?name=24254&title=测试")
//        val fragment = Router.from(uri).syncNavigation<Fragment>(this@MainActivity)
//        Router.from("/module1/method/test").forwardSync(this)
//        Executors.newSingleThreadExecutor().submit {
//            Router.from("/module1/method/test").forwardSync(this)
//            val fragment = Router.from(uri).syncNavigation<Fragment>(this@MainActivity)
//            if (fragment != null) {
//                LogUtil.log("enter this line 18487")
//                supportFragmentManager.beginTransaction().add(R.id.container, fragment)
//                    .commitAllowingStateLoss()
//            }
//        }
        GlobalScope.launch(Dispatchers.IO) {
           delay(1000)
            Router.from("/activity/navigation").forward()
        }
//

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}



