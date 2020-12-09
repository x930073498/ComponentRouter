package com.x930073498.bean

import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.annotations.FragmentAnnotation
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.annotations.ServiceAnnotation
import com.x930073498.util.getGroupFromPath

data class RouterInfo(val path: String, val group: String)

fun ActivityAnnotation.toInfo(): RouterInfo? {
    val group = (if (group.isEmpty()) getGroupFromPath(path) else group) ?: return null
    return RouterInfo(path, group)
}

fun FragmentAnnotation.toInfo(): RouterInfo? {
    val group = (if (group.isEmpty()) getGroupFromPath(path) else group) ?: return null
    return RouterInfo(path, group)
}

fun MethodAnnotation.toInfo(): RouterInfo? {
    val group = (if (group.isEmpty()) getGroupFromPath(path) else group) ?: return null
    return RouterInfo(path, group)
}

fun ServiceAnnotation.toInfo(): RouterInfo? {
    val group = (if (group.isEmpty()) getGroupFromPath(path) else group) ?: return null
    return RouterInfo(path, group)
}