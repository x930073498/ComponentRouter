package com.x930073498.component

import android.Manifest
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.just.agentweb.AgentWebConfig
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.ValueAutowiredAnnotation
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.Router
import com.x930073498.component.router.coroutines.result
import com.x930073498.component.router.navigator.impl.getInstanceService
import com.x930073498.component.router.request
import com.x930073498.component.router.requestService
import com.x930073498.component.router.scopeService
import com.x930073498.component.test.TestService
import com.x930073498.component.test.TestServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Sp<T> : SparseArray<T>()

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
            lifecycleScope.launch {
//                val job = launch {
//                    LogUtil.log("enter this line awawawa")
//                    val service = withContext(Dispatchers.IO) {
//                        Router.from("/test/service?testA=enter this line 123")
//                            .scopeService {
//                                delay(1000)
//                            }
//                            .getInstanceService<TestService>()
//                            .result()
//                    }
//                    LogUtil.log("enter this line 989f7444")
//                    service.test()
//                }
                Router.create<TestServiceImpl>().requestService() {
                    appendQuery("testA","enter this line 3as")
                }?.apply {
                    test()
                    invoke()
                }
            }


//            Router.from("/test/service?testA=enter this line 123")
//                .requestDirectAsService(2000L)
//                ?.getService<TestService>()?.apply {
//                    test()
//                    lifecycleScope.launch {
//                        invoke()
//                    }
//                }
//            Router.from("/test/service?testA=enter this line 123")
//                .asService(
//                    scope = lifecycleScope,
//                    coroutineContext = Dispatchers.IO,
//                    navigatorOption = NavigatorOption.ServiceNavigatorOption(true),
//                    context = view.context
//                ) {
//                    interceptors("/test/interceptors/test2")
//                }
//                .navigate()
//                .listen {
//                    LogUtil.log("isMainThread=$isMainThread,thread=${Thread.currentThread()}")
//                    it.asService().getServiceInstance<TestService>().test()
//                }.flatMap {
//                    Router.from("/activity/second")
//                        .scopeActivity {
//                            val array = Sp<Data>()
//                            array[1] = Data("name")
//                            bundle("array", array)
//                        }
//                        .navigateForActivityResult(this@MainActivity)
//                }.forceEnd {
//                    LogUtil.log("enter this line result=${it.data?.getStringExtra("result")}")
//                }
//                .bindLifecycle(this)

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



