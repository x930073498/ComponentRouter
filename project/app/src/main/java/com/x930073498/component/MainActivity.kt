package com.x930073498.component

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.just.agentweb.AgentWebConfig
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.Router
import com.x930073498.component.router.coroutines.bindLifecycle
import com.x930073498.component.router.coroutines.flatMap
import com.x930073498.component.router.coroutines.forceEnd
import com.x930073498.component.router.navigator.NavigatorOption
import com.x930073498.component.router.navigator.getServiceInstance
import com.x930073498.component.test.TestService
import kotlinx.coroutines.Dispatchers


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

        findViewById<View>(Window.ID_ANDROID_CONTENT).setOnClickListener { view ->
                Router.from("/test/service?testA=enter this line 123")
                    .asService(
                        scope = lifecycleScope,
                        coroutineContext = Dispatchers.IO,
                        navigatorOption = NavigatorOption.ServiceNavigatorOption(true),
                        context = view.context
                    )
                    .navigate()
                    .listen {
                        LogUtil.log("isMainThread=$isMainThread,thread=${Thread.currentThread()}")
                        it.asService().getServiceInstance<TestService>().test()
                    }.flatMap {
                        Router.from("/activity/second")
                            .asActivity()
                            .navigateForActivityResult(this@MainActivity)
                    }.forceEnd {
                        LogUtil.log("enter this line result=${it.data?.getStringExtra("result")}")
                    }
                    .bindLifecycle(this)

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



