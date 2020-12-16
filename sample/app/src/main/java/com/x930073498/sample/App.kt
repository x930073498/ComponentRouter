package com.x930073498.sample

import android.app.Application
import com.x930073498.component.auto.ConfigurationHandler
import com.x930073498.component.auto.IAuto
import com.x930073498.component.auto.IConfiguration

class App:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
class Configuration:IConfiguration(),IAuto{
    override fun ConfigurationHandler.config() {
        debug()
    }

}
