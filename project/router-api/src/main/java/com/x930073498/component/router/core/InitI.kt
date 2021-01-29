package com.x930073498.component.router.core

import android.app.Application
import com.x930073498.component.auto.Action

interface InitI:Action {
    fun init(app: Application): InitI

    fun checkRouteUnique(checkKeyUnique: Boolean): InitI

    fun fragmentPropertyAutoInject(autoInjectProperty:Boolean):InitI

    fun activityPropertyAutoInject(autoInjectProperty: Boolean):InitI

}

