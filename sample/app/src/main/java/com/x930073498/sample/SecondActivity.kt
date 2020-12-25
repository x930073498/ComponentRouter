package com.x930073498.sample

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.router.util.ParameterSupport
import com.x930073498.sample.databinding.ActivitySecondBinding

/**
 *
 */
@ActivityAnnotation(path = "/app/activity/second")
class SecondActivity : AppCompatActivity(R.layout.activity_second) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     val binding=  ActivitySecondBinding.bind(findViewById(R.id.container))
        binding.tv.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra("result","第二个activity"))
            finish()
        }
    }
}