package com.x930073498.component

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.just.agentweb.AgentWebConfig
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.*
import com.x930073498.component.router.response.asActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@ActivityAnnotation(path = "/test/test")
//@AndroidEntryPoint
class MainActivity : Activity() {
//    @Inject
//    lateinit var app: Application
//
//    @Inject
//    lateinit var foo: ActivityFoo

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
            GlobalScope.launch(Dispatchers.IO) {
                val result = Router.from("/activity/second").request().asActivity()
                    .navigateForActivityResult()
                LogUtil.log("enter this line result=${result.data?.getStringExtra("result")}")
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



