package com.x930073498.sample

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Data(@SerializedName("name") val name: String)

class S : Serializable {
    private var a = ""

    fun www(a:String){
        this.a=a
    }

    fun uuu():String{
        return a
    }


}
class W : Serializable {
    private var a = ""

    fun www(a:String){
        this.a=a
    }

    fun uuu():String{
        return a
    }


}