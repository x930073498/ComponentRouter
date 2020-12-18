package com.x930073498.sample

import java.io.Serializable


data class Data(val name: String)

class S : Serializable {
     var a = ""

    fun www(a:String){
        this.a=a
    }

    fun uuu():String{
        return a
    }


}
class W : Serializable {
     var a = ""

    fun www(a:String){
        this.a=a
    }

    fun uuu():String{
        return a
    }


}