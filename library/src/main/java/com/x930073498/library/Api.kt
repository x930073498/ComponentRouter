package com.x930073498.library

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

var isEnable: Boolean? = null
val a=AtomicBoolean()


val executor=Executors.newSingleThreadExecutor()