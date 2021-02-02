@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router.action

import android.app.Activity
import android.net.Uri
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.ActivityAnnotation
import com.x930073498.component.annotations.FragmentAnnotation
import com.x930073498.component.annotations.ServiceAnnotation
import com.x930073498.component.annotations.realPath
import com.x930073498.component.router.Router
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.ServiceActionDelegate
import com.x930073498.component.router.impl.SystemActionDelegate
import com.x930073498.component.router.util.authorityAndPath
import java.util.concurrent.locks.ReentrantLock


/**
 * 用于记录路由是否被注册
 */
sealed class PathState {
    object NONE : PathState()
    class LOADED(val delegate: ActionDelegate) : PathState()
    class UNLOADED(val delegate: ActionDelegate) : PathState()
}

interface ModuleHandle {
    fun getRealPathFromTarget(target: Any): String?
    fun getPathState(group: String, path: String, isRealPath: Boolean = false): PathState
    fun getRealPathState(realPath: String): PathState
    fun unloadGroup(group: String): Boolean
    fun loadGroup(group: String): Boolean
    fun unloadRealPath(realPath: String): Boolean
    fun loadRealPath(realPath: String): Boolean
    fun unloadWithGroupAndPath(group: String, path: String, isRealPath: Boolean = false): Boolean
    fun loadWithGroupAndPath(group: String, path: String, isRealPath: Boolean = false): Boolean
}

object ActionCenter {

    internal val moduleHandler = object : ModuleHandle {

        override fun getRealPathFromTarget(target: Any): String? {
            when (target) {
                is Activity -> {
                    val annotation =
                        target::class.java.getAnnotation(ActivityAnnotation::class.java)
                            ?: return null
                    return annotation.realPath()
                }
                is Fragment -> {
                    val annotation =
                        target::class.java.getAnnotation(FragmentAnnotation::class.java)
                            ?: return null
                    return annotation.realPath()
                }
                is IService -> {
                    val annotation =
                        target::class.java.getAnnotation(ServiceAnnotation::class.java)
                            ?: return null
                    return annotation.realPath()
                }
            }
            return null
        }

        override fun getPathState(group: String, path: String, isRealPath: Boolean): PathState {
            return ActionCenter.getPathState(group, path, isRealPath)
        }

        override fun getRealPathState(realPath: String): PathState {
            return ActionCenter.getRealPathState(realPath)
        }

        override fun unloadGroup(group: String): Boolean {
            return ActionCenter.unloadGroup(group)
        }

        override fun loadGroup(group: String): Boolean {
            return ActionCenter.loadGroup(group)
        }

        override fun unloadRealPath(realPath: String): Boolean {
            return ActionCenter.unloadRealPath(realPath)
        }

        override fun loadRealPath(realPath: String): Boolean {
            return ActionCenter.loadRealPath(realPath)
        }

        override fun unloadWithGroupAndPath(
            group: String,
            path: String,
            isRealPath: Boolean
        ): Boolean {
            return ActionCenter.unloadWithGroupAndPath(group, path, isRealPath)
        }

        override fun loadWithGroupAndPath(
            group: String,
            path: String,
            isRealPath: Boolean
        ): Boolean {
            return ActionCenter.loadWithGroupAndPath(group, path, isRealPath)
        }

    }

    /**
     * key1 group,key2 path
     */
    private val loadedMap = arrayMapOf<String, ArrayMap<String, ActionDelegate>>()
    private val unloadedMap = arrayMapOf<String, ArrayMap<String, ActionDelegate>>()

    internal var checkKeyUnique = false
    private val lock = ReentrantLock()


    private fun unloadGroup(group: String): Boolean {
        if (lock.tryLock()) {
            lock.lock()
            val removed = loadedMap.remove(group) ?: run {
                lock.unlock()
                return unloadedMap.containsKey(group)
            }
            var groupMap = unloadedMap[group]
            if (groupMap == null) {
                groupMap = arrayMapOf()
                unloadedMap[group] = groupMap
            }
            groupMap.putAll(removed as Map<String, ActionDelegate>)
            lock.unlock()
            return true
        } else {
            return false
        }

    }

    private fun loadGroup(group: String): Boolean {
        if (lock.tryLock()) {
            lock.lock()
            val removed = unloadedMap.remove(group) ?: run {
                lock.unlock()
                return loadedMap.containsKey(group)
            }
            var groupMap = loadedMap[group]
            if (groupMap == null) {
                groupMap = arrayMapOf()
                loadedMap[group] = groupMap
            }
            groupMap.putAll(removed as Map<String, ActionDelegate>)
            lock.unlock()
            return true
        }
        return false
    }

    private fun unloadRealPath(realPath: String): Boolean {
        val group = getGroupFromPath(realPath) ?: return false
        return unloadWithGroupAndPath(group, realPath, true)
    }

    private fun loadRealPath(realPath: String): Boolean {
        val group = getGroupFromPath(realPath) ?: return false
        return loadWithGroupAndPath(group, realPath, true)

    }

    private fun unloadWithGroupAndPath(
        group: String,
        path: String,
        isRealPath: Boolean = false
    ): Boolean {
        if (lock.tryLock()) {
            lock.lock()
            val realPath = if (isRealPath) path else "/$group$path"
            val loadedGroup = loadedMap[group] ?: run {
                lock.unlock()
                return getPathState(group, path, isRealPath) is PathState.UNLOADED
            }
            val removed = loadedGroup.remove(realPath) ?: run {
                lock.unlock()
                return getPathState(group, path, isRealPath) is PathState.UNLOADED
            }
            var unloadedGroup = unloadedMap[group]
            if (unloadedGroup == null) {
                unloadedGroup = arrayMapOf()
                unloadedMap[group] = unloadedGroup
            }
            unloadedGroup[realPath] = removed
            lock.unlock()
            return true
        }
        return false

    }

    private fun loadWithGroupAndPath(
        group: String,
        path: String,
        isRealPath: Boolean = false
    ): Boolean {
        if (lock.tryLock()) {
            lock.lock()
            val realPath = if (isRealPath) path else "/$group$path"
            val unloadGroup = unloadedMap[group] ?: run {
                lock.unlock()
                return getPathState(group, path, isRealPath) is PathState.LOADED
            }
            val removed = unloadGroup.remove(realPath) ?: run {
                lock.unlock()
                return getPathState(group, path, isRealPath) is PathState.LOADED
            }
            var loadedGroup = loadedMap[group]
            if (loadedGroup == null) {
                loadedGroup = arrayMapOf()
                loadedMap[group] = loadedGroup
            }
            loadedGroup[realPath] = removed
            lock.unlock()
            return true
        }
        return false
    }

    private fun register(
        actionDelegate: ActionDelegate,
        map: ArrayMap<String, ArrayMap<String, ActionDelegate>>
    ) {
        val path = actionDelegate.path
        val group = actionDelegate.group
        var groupMap = map[group]
        if (groupMap == null) {
            groupMap = arrayMapOf()
            map[group] = groupMap
        }
        groupMap[path] = actionDelegate
    }


    private fun getPathState(group: String, path: String, isRealPath: Boolean): PathState {
        val realPath = if (isRealPath) path else "/$group$path"
        var delegate = getDelegateFromMap(loadedMap, group, realPath)
        if (delegate != null) return PathState.LOADED(delegate)
        delegate = getDelegateFromMap(unloadedMap, group, realPath)
        if (delegate != null) return PathState.UNLOADED(delegate)
        return PathState.NONE
    }

    private fun getRealPathState(realPath: String): PathState {
        val group = getGroupFromPath(realPath) ?: return PathState.NONE
        return getPathState(group, realPath, true)
    }

    private fun checkUnique(group: String, path: String, actionDelegate: ActionDelegate) {
        if (checkKeyUnique) {
            val state = getPathState(group, path, true)
            if (state is PathState.UNLOADED) {
                throw RuntimeException("路由冲突,$actionDelegate 与 ${state.delegate} 冲突")
            } else if (state is PathState.LOADED) {
                throw RuntimeException("路由冲突,$actionDelegate 与 ${state.delegate} 冲突")
            }
        }
    }

    private fun getDelegateFromMap(
        map: ArrayMap<String, ArrayMap<String, ActionDelegate>>,
        group: String?,
        path: String?
    ): ActionDelegate? {
        if (group.isNullOrEmpty()) return null
        if (path.isNullOrEmpty()) return null
        return map[group]?.get(path)
    }


    fun register(actionDelegate: ActionDelegate) {
        val autoRegister = actionDelegate.autoRegister
        checkUnique(actionDelegate.group, actionDelegate.path, actionDelegate)
        register(actionDelegate, if (autoRegister) loadedMap else unloadedMap)
    }


    internal fun getAction(url: String): ActionDelegate {
        val key = Uri.parse(url).authorityAndPath()
        val group = getGroupFromPath(key.path)
        return getDelegateFromMap(loadedMap, group, key.path) ?: SystemActionDelegate()
    }


    fun <T> getService(clazz: Class<T>): T? where T : IService {
        return getServiceInternal(clazz)
    }

    fun <T> getService(path: String): T? where T : IService {
        val action = getAction(path)
        return if (action is ServiceActionDelegate) {
            action.factory()
                .create(ContextHolder.create(), action.target.targetClazz, bundleOf()) as? T
        } else null

    }


    private fun <T> getServiceInternal(clazz: Class<T>): T? where T : IService {
        if (!Router.hasInit) {
            throw RuntimeException("Router 尚未初始化成功")
        }
        if (lock.tryLock()) {
            lock.lock()
            loadedMap.values.forEach { map ->
                map.values.forEach {
                    with(it.target) {
                        if (this is Target.ServiceTarget && clazz.isAssignableFrom(targetClazz)) {
                            lock.unlock()
                            return action.factory().create(
                                ContextHolder.create(), action.target.targetClazz,
                                bundleOf()
                            ) as? T
                        }
                    }
                }
            }
            lock.unlock()
        }
        return null

    }


    internal fun getAction(uri: Uri): ActionDelegate {
        val key = uri.authorityAndPath()
        val group = getGroupFromPath(key.path)
        return getDelegateFromMap(loadedMap, group, key.path) ?: SystemActionDelegate()
    }


    private fun getGroupFromPath(path: String?): String? {
        val group = runCatching { path?.substring(1, path.indexOf("/", 1)) }.onFailure {
        }.getOrNull() ?: return null
        if (group.isEmpty()) return null
        return group
    }


}