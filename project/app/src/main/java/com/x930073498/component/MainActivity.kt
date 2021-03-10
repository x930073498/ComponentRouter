package com.x930073498.component

import android.Manifest
import android.content.Intent
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
import com.x930073498.component.fragmentation.loadRootFromRouter
import com.x930073498.component.router.*
import com.x930073498.component.router.coroutines.end
import com.x930073498.component.router.coroutines.result
import com.x930073498.component.test.TestParentService1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Sp<T> : SparseArray<T>()

@ActivityAnnotation(path = "/test/test")
class MainActivity : AppCompatActivity() {


//    private val viewModel by lazy {
//        ViewModelProvider(this)[MainViewModel::class.java]
//    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return
        val uri = intent.data ?: return
        loadRootFromRouter(Window.ID_ANDROID_CONTENT, uri.toString(), lifecycleScope) {
            this.withRouter {
                bundle {
                    putAll(intent.extras)
                }
            }
        }
    }

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
//                Router.create<TestService1>().requestService() {
//                    appendQuery("testA","enter this line 3as")
//                }?.apply {
//                    test()
//                    invoke()
//                }
//               Router.from("/test/service/1").asService {
//                    addInterceptor("/test/interceptors/test3")
//                }.navigate().await()
                Router.from("/test/service/1").navigate(lifecycleScope) {
//                    addInterceptor("/test/interceptors/test3")
//                    addInterceptor("/interceptor/toActivitySecond")
                }
                LogUtil.log("enter this line tttt")

                Router.from("/activity/navigation").navigate(lifecycleScope).await()
//                delay(2000)
//                LogUtil.log("777777777777777777777777777777")
//                Router.from("/test/service/1").requestDirectAsService {
//                    addInterceptor("/test/interceptors/test3")
//                }?.getService<TestParentService1>()
            }.invokeOnCompletion {
                LogUtil.log("enter this line sasa")
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
        onNewIntent(intent)
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



