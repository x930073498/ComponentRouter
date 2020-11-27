package com.zx.common.auto

import android.app.Application

interface IApplicationLifecycle {
    fun onApplicationCreated(app: Application)
}
