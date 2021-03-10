package com.x930073498.component.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.x930073498.component.R
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.LaunchMode
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.fragmentation.loadRootFromRouter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ActivityAnnotation(
    path = "/activity/navigation",
    launchMode = LaunchMode.SingleTop
)
class NavigationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        loadRootFromRouter(R.id.container, "/test/a?name=24254&title=测试", scope = lifecycleScope)

    }



}