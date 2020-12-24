package com.x930073498.component.annotations

private fun getGroupFromPath(path: String?): String {
    if (path.isNullOrEmpty()) return ""
    if (path.startsWith("/")) {
        val nextIndex = path.indexOf("/", 1)
        if (nextIndex <= 2) return ""
        return path.substring(1, nextIndex)
    }
    return ""
}

private fun getRealGroup(group: String, path: String): String {
    return if (group.isEmpty()) getGroupFromPath(path) else group
}

private fun getRealPath(group: String, path: String): String {
    return if (group.isEmpty()) path else "/$group$path"
}

fun FragmentAnnotation.realPath(): String {
    return getRealPath(group, path)
}
fun FragmentAnnotation.realGroup(): String {
    return getRealGroup(group, path)
}

fun ActivityAnnotation.realPath(): String {
    return getRealPath(group, path)
}
fun ActivityAnnotation.realGroup(): String {
    return getRealGroup(group, path)
}

fun MethodAnnotation.realPath(): String {
    return getRealPath(group, path)
}
fun MethodAnnotation.realGroup(): String {
    return getRealGroup(group, path)
}

fun ServiceAnnotation.realPath(): String {
    return getRealPath(group, path)
}
fun ServiceAnnotation.realGroup(): String {
    return getRealGroup(group, path)
}

fun InterceptorAnnotation.realPath(): String {
    return getRealPath(group, path)
}
fun InterceptorAnnotation.realGroup(): String {
    return getRealGroup(group, path)
}