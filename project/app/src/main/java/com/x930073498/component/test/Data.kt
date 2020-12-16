package com.x930073498.component.test

import com.alibaba.fastjson.annotation.JSONField


data class Data(
    @JSONField(name = "name")
    val name: String
)