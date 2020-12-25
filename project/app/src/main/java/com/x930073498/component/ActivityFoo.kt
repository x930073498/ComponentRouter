package com.x930073498.component

import android.content.Context
import android.util.SparseArray
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.router.util.*
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.Serializable
import javax.inject.Inject

class D : Serializable
class ActivityFoo @Inject constructor(@ActivityContext val context: Context) {
    fun test() {
        val list: List<D> = mutableListOf()
        val b: Any = "SparseArray<CharSequence>()"
        val c = SparseArray<String>()
        runCatching {


            LogUtil.log(
                "enter this line isArrayList=${
                    list.getType().isArrayListOf(Serializable::class.java)
                }"
            )
            LogUtil.log("enter this line isSubtypeOf=${c.getType().isSubtypeOf<SparseArray<CharSequence>>()}")
            LogUtil.log(
                "enter this line isList=${
                    list.getType().isListOf(Serializable::class.java)
                }"
            )
            LogUtil.log(
                "enter this line isAIsList=${
                    "666".getType().isListOf(Serializable::class.java)
                }"
            )
        }.onFailure { it.printStackTrace() }
    }
}