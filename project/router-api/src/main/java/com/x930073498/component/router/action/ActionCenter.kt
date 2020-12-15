@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router.action

import android.net.Uri
import androidx.core.os.bundleOf
import com.x930073498.component.router.Router
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.ServiceActionDelegate
import com.x930073498.component.router.impl.SystemActionDelegate
import com.x930073498.component.router.util.authorityAndPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

object ActionCenter {

    internal var checkKeyUnique = false

    /**
     * 存储所有的action
     */
    private val map = mutableMapOf<Key, ActionDelegate>()

    /**
     * 存储可用的action
     */
    private val mMap = mutableMapOf<Key, ActionDelegate>()


    fun register(actionDelegate: ActionDelegate): Key {
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


    internal fun getAction(url: String): ActionDelegate {
        val key = Uri.parse(url).authorityAndPath()
        val group = getGroupFromPath(key.path)
        return mMap[Key(group, key.path)] ?: SystemActionDelegate()
    }


    fun <T> getServiceSync(clazz: Class<T>): T? where T : IService {
        return getServiceSyncInternal(clazz)
    }

    suspend fun <T> getService(clazz: Class<T>): T? where T : IService {
        return getServiceInternal(clazz)
    }

    private fun <T> getServiceSyncInternal(clazz: Class<T>): T? where T : IService {
        if (!Router.hasInit) {
            throw RuntimeException("Router 尚未初始化成功")
        }
        val action = mMap.values.firstOrNull {
            with(it.target) {
                this is Target.ServiceTarget && clazz.isAssignableFrom(targetClazz)
            }
        } as? ServiceActionDelegate ?: return null
        val resultRef = AtomicReference<T>()
        val flagRef = AtomicReference(0)
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                resultRef.set(
                    action.factory()
                        .create(ContextHolder.create(), action.target.targetClazz, bundleOf()) as? T
                )
            }
            flagRef.set(1)
        }
        while (flagRef.get() == 0) {
            // do nothing
        }
        return resultRef.get()
    }

    private suspend fun <T : IService> getServiceInternal(clazz: Class<T>): T? {
        if (!Router.hasInit) {
            throw RuntimeException("Router 尚未初始化成功")
        }
        val action = mMap.values.firstOrNull {
            with(it.target) {
                this is Target.ServiceTarget && clazz.isAssignableFrom(targetClazz)
            }
        } as? ServiceActionDelegate ?: return null
        return action.factory()
            .create(ContextHolder.create(), action.target.targetClazz, bundleOf()) as? T
    }

   internal  fun getAction(uri: Uri): ActionDelegate {
        val key = uri.authorityAndPath()
        val group = getGroupFromPath(key.path)
        return mMap[Key(group, key.path)] ?: SystemActionDelegate()
    }


    private fun getGroupFromPath(path: String?): String? {
        val group = runCatching { path?.substring(1, path.indexOf("/", 1)) }.onFailure {
        }.getOrNull() ?: return null
        if (group.isEmpty()) return null
        return group
    }


}