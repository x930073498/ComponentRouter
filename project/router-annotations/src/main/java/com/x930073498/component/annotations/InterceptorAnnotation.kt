package com.x930073498.component.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class InterceptorAnnotation(
    val path: String,
    val group: String = "",
    val autoRegister:Boolean=true,
    val scope: InterceptorScope = InterceptorScope.NORMAL
)

enum class InterceptorScope {
    NORMAL,//只在使用的时候有效
    GLOBAL,//只在全局中有效，使用插件会自动注册
    ALL//所有环境皆可使用,不建议使用，这会导致一个拦截器可能会拦截两次
}