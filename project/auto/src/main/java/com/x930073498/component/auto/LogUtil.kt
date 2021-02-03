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
    internal var debug = true
    private var logger: Logger = Logger.Companion
    fun setLogger(logger: Logger) {
        this.logger = logger
    }

    @JvmStatic
    fun log(msg: Any?) {
        if (debug) {
            if (msg == null) return
            logger.log("x930073498-component", msg)
        }

    }

}