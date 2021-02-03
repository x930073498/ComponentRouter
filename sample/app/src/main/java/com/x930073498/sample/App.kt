package com.x930073498.sample

import android.app.Application
import android.util.Log
import com.x930073498.component.auto.*
import com.x930073498.component.core.app
import com.x930073498.component.router.byRouter

class App : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}

class Configuration : IConfiguration, IAuto {
    override fun option(holder: ConfigurationHolder) {

        holder.byDefault {
            setLogger { tag, msg ->
                Log.d(tag, serializer?.serialize(msg) ?: msg.toString())
            }
            setDebug(true)
        }
        holder.byRouter {
            checkRouteUnique(true)
            fragmentPropertyAutoInject(true)
            activityPropertyAutoInject(true)
        }
    }


}
