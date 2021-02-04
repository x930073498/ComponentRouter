package com.x930073498.component.auto


internal var isDebug = false
internal var iSerializer: ISerializer? = null

internal class DefaultHandleAction : HandleAction {
    override fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    override fun setSerializer(serializer: ISerializer) {
        if (iSerializer != null) return
        iSerializer = serializer
    }

    override fun setLogger(logger: Logger) {
        LogUtil.setLogger(logger)
    }

    override fun setLogTag(tag: String) {
        LogUtil.setDefaultLogTag(tag)
    }


}