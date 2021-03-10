package com.x930073498.component.router.util

import com.x930073498.component.router.compiler.ActivityInfo
import com.x930073498.component.router.compiler.FragmentInfo
import com.x930073498.component.router.compiler.InterceptorInfo
import com.x930073498.component.router.compiler.ServiceInfo
import com.x930073498.component.router.data.RouterDoc

fun ActivityInfo.collectDoc() {
    if (processor.isDocEnable) {
        processor.docList.add(
            RouterDoc(
                group,
                path,
                type.toString(),
                "activity",
                annotation.desc,
                autoInjectList.map {
                    it.toParams()
                })
        )
    }
}

fun FragmentInfo.collectDoc() {
    if (processor.isDocEnable) {
        processor.docList.add(
            RouterDoc(
                group,
                path,
                type.toString(),
                "fragment",
                annotation.desc,
                autoInjectList.map {
                    it.toParams()
                })
        )
    }
}

fun InterceptorInfo.collectDoc() {
    if (processor.isDocEnable) {
        processor.docList.add(
            RouterDoc(
                group,
                path,
                type.toString(),
                "interceptor",
                annotation.desc,
                autoInjectList.map {
                    it.toParams()
                })
        )
    }
}
fun ServiceInfo.collectDoc(){
    if (processor.isDocEnable){
        processor.docList.add(
            RouterDoc(
                group,
                path,
                type.toString(),
                "service",
                annotation.desc,
                autoInjectList.map {
                    it.toParams()
                })
        )
    }

}