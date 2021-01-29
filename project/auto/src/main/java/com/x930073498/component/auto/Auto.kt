package com.x930073498.component.auto

fun getSerializer(): ISerializer {
    return iSerializer?: throw RuntimeException("请先指定ISerializer")
}


