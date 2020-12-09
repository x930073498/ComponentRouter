package com.x930073498.common.auto

import android.app.Application

interface IAuto


val IAuto.app: Application
    get() {
        return AutoTaskRegister.app
    }