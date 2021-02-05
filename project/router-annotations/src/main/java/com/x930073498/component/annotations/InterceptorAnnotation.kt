package com.x930073498.component.annotations

import androidx.annotation.IntRange

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class InterceptorAnnotation(
    val path: String,
    val group: String = "",
    val desc: String = "",
    val autoRegister: Boolean = true,
    @IntRange(from = 0, to = Int.MAX_VALUE.toLong())
    val priority: Int = DEFAULT_PRIORITY,//仅当与globalInterceptor 比较排序时有用，当scope为Normal时不建议修改默认值
    val scope: InterceptorScope = InterceptorScope.NORMAL
) {
    companion object {
        const val DEFAULT_PRIORITY = 255
    }
}

enum class InterceptorScope {
    NORMAL,//只在使用的时候有效
    GLOBAL,//只在全局中有效，使用插件会自动注册
    ALL//所有环境皆可使用,不建议使用，这会导致一个拦截器可能会拦截两次
}