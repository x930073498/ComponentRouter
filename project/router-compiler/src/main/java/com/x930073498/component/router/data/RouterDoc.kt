package com.x930073498.component.router.data

data class RouterDoc(
    val group: String,//组别
    val path: String,//子路径
    val target: String,//目标路径
    val category: String,//类别，method，activity，fragment，interceptor，service
    val desc: String,//描述
    val params: List<Params>//参数列表
)


data class Params(
    val name: String,
    val type: String,
    val desc: String,
    val require: Boolean
)
