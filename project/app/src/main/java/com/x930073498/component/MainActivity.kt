package com.x930073498.component

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.just.agentweb.AgentWebConfig
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.Router
import com.x930073498.component.router.coroutines.bindLifecycle
import com.x930073498.component.router.coroutines.end
import com.x930073498.component.router.coroutines.flatMap
import com.x930073498.component.router.coroutines.map
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ActivityAnnotation(path = "/test/test")
class MainActivity : AppCompatActivity() {


//    private val viewModel by lazy {
//        ViewModelProvider(this)[MainViewModel::class.java]
//    }


    @ValueAutowiredAnnotation("name")
    var name: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        foo.test()
//        viewModel.test()
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
        AgentWebConfig.debug()

        findViewById<View>(Window.ID_ANDROID_CONTENT).setOnClickListener {
            val handle = Router.from("/activity/second")
                .asActivity()
                .navigateForActivityResult()
                .end {
                    LogUtil.log("enter this line result=${it.data?.getStringExtra("result")}")
                }
                .bindLifecycle(this)

            GlobalScope.launch {
                delay(1000)
                LogUtil.log("enter this line hasResult=${handle.hasResult()}")
                handle.cancel()
            }
        }

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



