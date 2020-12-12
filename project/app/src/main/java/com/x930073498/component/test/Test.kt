package com.x930073498.component.test

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.annotations.MethodBundleNameAnnotation
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
    println("enter this line 897 ${a + b + c}$context")

}

//@MethodAnnotation(path = "/test/test4", group = "a")
@MethodAnnotation(path = "/test/method/test1")
suspend fun testMethod2(context: Context) {
    delay(1000)
    println("enter this line 877 $context")

}

@UiThread
@MethodAnnotation(path = "/method/toast")
fun toast(context: Context, msg: String?) {
    if (msg==null)return
    Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
}



