package com.x930073498.router.action

import android.net.Uri
import com.x930073498.router.impl.ActionDelegate
import com.x930073498.router.util.authorityAndPath

object ActionCenter {

    var checkKeyUnique = false

    /**
     * 存储所有的action
     */
    private val map = mutableMapOf<Key, ActionDelegate<*>>()

    /**
     * 存储可用的action
     */
    private val mMap = mutableMapOf<Key, ActionDelegate<*>>()


    fun register(actionDelegate: ActionDelegate<*>): Key {
        val key = Key(actionDelegate.group, actionDelegate.path)
        if (checkKeyUnique) {
            val last = map[key]
            if (last != null) {
                throw RuntimeException("key $key 冲突,$last 与 $actionDelegate 冲突")
            }
        }
        map[key] = actionDelegate
        mMap[key] = actionDelegate
        return key
    }

    fun reloadGroup(group: String) {
        mMap.putAll(map.filter { it.key.group == group })
    }

    fun unregister(key: Key) {
        mMap.remove(key)
    }

    fun unregister(authority: String, path: String) {
        unregister(Key(authority, path))
    }

    fun unloadGroup(group: String) {
        mMap.keys.filter { it.group == group }.forEach {
            mMap.remove(it)
        }
    }


    internal fun getAction(url: String): ActionDelegate<*>? {
        val key = Uri.parse(url).authorityAndPath()
        return mMap[Key(key.authority, key.path)]
    }


    internal fun getAction(uri: Uri): ActionDelegate<*>? {
        val key = uri.authorityAndPath()
        return mMap[Key(key.authority, key.path)]
    }


}