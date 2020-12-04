package com.x930073498.router.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.x930073498.router.action.Target

interface ActivityActionDelegate : ActionDelegate {
    suspend fun target(): Target.ActivityTarget
    fun inject(bundle: Bundle, target: Activity)
}