package com.zx.common.auto

import android.app.Application

interface IAuto


val IAuto.app: Application
    get() {
        return AutoTaskRegister.app
    }