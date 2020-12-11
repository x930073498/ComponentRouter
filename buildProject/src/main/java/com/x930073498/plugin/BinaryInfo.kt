package com.x930073498.plugin

object BinaryInfo {


    var enable: Boolean = true
    var userOrg: String? = ""
    var repoName: String? = ""
    var bintrayUser: String? = ""
    var bintrayKey: String? = ""

    fun valid(): Boolean {

        return !(userOrg.isNullOrEmpty() || repoName.isNullOrEmpty() || bintrayKey.isNullOrEmpty()
                || bintrayUser.isNullOrEmpty())&& enable
    }

    override fun toString(): String {
        return "{enable=$enable,userOrg=$userOrg,repoName=$repoName,bintrayUser=$bintrayUser,bintrayKey=$bintrayKey}"
    }


}