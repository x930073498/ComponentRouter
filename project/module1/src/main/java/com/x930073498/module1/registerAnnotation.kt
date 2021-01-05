package com.x930073498.module1

import com.x930073498.component.auto.annotations.ClassInjector
import com.x930073498.component.auto.LogUtil

@ClassInjector("annotation")
 fun registerAnnotation(data: Any?) {
    LogUtil.log("enter this line $data")
}
