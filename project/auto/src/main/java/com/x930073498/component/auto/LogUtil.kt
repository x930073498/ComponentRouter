package com.x930073498.component.auto


fun interface Logger {
    fun log(tag: String, msg: Any?)

    companion object : Logger {
        override fun log(tag: String, msg: Any?) {
            println("$tag {$msg}")
        }
    }
}

object LogUtil {
    private var defaultTag="x930073498-component"
    internal var debug = true
    private var logger: Logger = Logger.Companion
    fun setDefaultLogTag(tag:String){
        defaultTag=tag
    }
    fun setLogger(logger: Logger) {
        this.logger = logger
    }

    @JvmStatic
    fun log(msg: Any?) {
        if (debug) {
            if (msg == null) return
            logger.log(defaultTag, msg)
        }

    }

}