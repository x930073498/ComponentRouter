package com.x930073498.router.action

data class Key(val group: String?, val path: String?){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Key

        if (group != other.group) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group?.hashCode() ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        return result
    }
}