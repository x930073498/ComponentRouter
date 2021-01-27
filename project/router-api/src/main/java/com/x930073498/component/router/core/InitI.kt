package com.x930073498.component.router.core

import android.app.Application

interface InitI {
    fun init(app: Application): InitI

    fun checkRouteUnique(checkKeyUnique: Boolean): InitI
}