package com.x930073498.component.test

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.alibaba.fastjson.annotation.JSONField
import com.x930073498.component.annotations.MethodAnnotation
import com.x930073498.component.annotations.MethodBundleNameAnnotation
import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.annotation.concurrent.ThreadSafe


@MethodAnnotation(path = "/test/test4", group = "a", interceptors = ["/test/interceptors/test1"])
suspend fun testMethod(
    @MethodBundleNameAnnotation("context") context: Context,
    @MethodBundleNameAnnotation("a") a: String?,
    @MethodBundleNameAnnotation("b") b: Int?,
    @MethodBundleNameAnnotation("c") c: CharSequence,
) {
    delay(1200)
    LogUtil.log("enter this line 897 ${a + b + c}$context")

}

//@MethodAnnotation(path = "/test/test4", group = "a")
@MethodAnnotation(path = "/test/method/test1")
suspend fun testMethod2(context: Context) {
    delay(1000)
    LogUtil.log("enter this line 877 $context")

}

@UiThread
@MethodAnnotation(path = "/method/toast")
fun toast(context: Context, info: ToastInfo?) {
    if (info==null)return
    Toast.makeText(context.applicationContext, info.msg, info.duration).show()
}

//data class ToastInfo(@JSONField(name = "msg")val msg:String, @JSONField(name = "duration")val duration:Int =Toast.LENGTH_SHORT)
data class ToastInfo(@JSONField(name = "msg")val msg:String, @JSONField(name = "duration")val duration:Int=Toast.LENGTH_SHORT)
