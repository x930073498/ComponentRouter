@file:Suppress("UNCHECKED_CAST")

package com.x930073498.component.router.action

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.x930073498.component.annotations.*
import com.x930073498.component.auto.LogUtil
import com.x930073498.component.core.isMainThread
import com.x930073498.component.router.Router
import com.x930073498.component.router.core.DirectRequestResult
import com.x930073498.component.router.impl.ActionDelegate
import com.x930073498.component.router.impl.IService
import com.x930073498.component.router.impl.ServiceActionDelegate
import com.x930073498.component.router.impl.SystemActionDelegate
import com.x930073498.component.router.thread.IThread
import com.x930073498.component.router.util.ParameterSupport
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


    private fun unloadGroup(group: String): Boolean {
        val removed = loadedMap.remove(group) ?: run {
            return unloadedMap.containsKey(group)
        }
        var groupMap = unloadedMap[group]
        if (groupMap == null) {
            groupMap = arrayMapOf()
            unloadedMap[group] = groupMap
        }
        groupMap.putAll(removed as Map<String, ActionDelegate>)
        return true


    }

    private fun loadGroup(group: String): Boolean {
        val removed = unloadedMap.remove(group) ?: run {
            return loadedMap.containsKey(group)
        }
        var groupMap = loadedMap[group]
        if (groupMap == null) {
            groupMap = arrayMapOf()
            loadedMap[group] = groupMap
        }
        groupMap.putAll(removed as Map<String, ActionDelegate>)
        return true
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
        val realPath = if (isRealPath) path else "/$group$path"
        val loadedGroup = loadedMap[group] ?: run {
            return getPathState(group, path, isRealPath) is PathState.UNLOADED
        }
        val removed = loadedGroup.remove(realPath) ?: run {
            return getPathState(group, path, isRealPath) is PathState.UNLOADED
        }
        var unloadedGroup = unloadedMap[group]
        if (unloadedGroup == null) {
            unloadedGroup = arrayMapOf()
            unloadedMap[group] = unloadedGroup
        }
        unloadedGroup[realPath] = removed
        return true


    }

    private fun loadWithGroupAndPath(
        group: String,
        path: String,
        isRealPath: Boolean = false
    ): Boolean {
        val realPath = if (isRealPath) path else "/$group$path"
        val unloadGroup = unloadedMap[group] ?: run {
            return getPathState(group, path, isRealPath) is PathState.LOADED
        }
        val removed = unloadGroup.remove(realPath) ?: run {
            return getPathState(group, path, isRealPath) is PathState.LOADED
        }
        var loadedGroup = loadedMap[group]
        if (loadedGroup == null) {
            loadedGroup = arrayMapOf()
            loadedMap[group] = loadedGroup
        }
        loadedGroup[realPath] = removed
        return true

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


    private fun checkThread(thread: IThread) {
        if (thread == IThread.UI && !isMainThread) {
            throw RuntimeException("请在主线程调用")
        } else if (thread == IThread.WORKER && isMainThread) {
            throw RuntimeException("请在子线程调用")
        }
    }


    internal fun <T> getTarget(clazz: Class<T>, bundle: Bundle, contextHolder: ContextHolder): T? {
        loadedMap.values.iterator().forEach { map ->
            map.values.iterator().forEach {
                with(it.target) {
                    if (clazz.isAssignableFrom(targetClazz)) {
                        when (this) {
                            is Target.ServiceTarget -> {
                                checkThread(action.thread)
                                return clazz.cast(
                                    action.factory().create(contextHolder, targetClazz, bundle)
                                        .apply {
                                            init(contextHolder, bundle)
                                            action.inject(bundle, this)
                                        }
                                )
                            }
                            is Target.MethodTarget -> {
                                checkThread(action.thread)
                                return clazz.cast(
                                    action.factory().create(contextHolder, targetClazz, bundle)
                                        .apply {
                                            action.inject(bundle, this)
                                        }
                                )
                            }
                            is Target.ActivityTarget -> return null
                            is Target.FragmentTarget -> {
                                checkThread(action.thread)
                                return clazz.cast(
                                    action.factory().create(contextHolder, targetClazz, bundle)
                                        .apply {
                                            action.inject(bundle, this)
                                        })
                            }
                            is Target.InterceptorTarget -> {
                                checkThread(action.thread)
                                return clazz.cast(
                                    action.factory().create(contextHolder, targetClazz).apply {
                                        action.inject(bundle, this)
                                    })
                            }
                            is Target.SystemTarget -> return null
                        }

                    }
                }
            }
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

    internal fun getResultDirect(
        uri: Uri,
        bundle: Bundle,
        contextHolder: ContextHolder
    ): DirectRequestResult {
        val action = getAction(uri)
        when (val target = action.target) {
            is Target.ServiceTarget -> {
                checkThread(target.action.thread)
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.ServiceResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.MethodTarget -> {
                checkThread(target.action.thread)
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.MethodResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.ActivityTarget -> {
                checkThread(target.action.thread)
                val actualContext = contextHolder.getContext()
                val intent = Intent(actualContext, target.targetClazz)
                val componentName = intent.resolveActivity(contextHolder.getPackageManager())
                if (componentName != null) {
                    intent.apply {
                        if (actualContext is Application) {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        } else {
                            when (target.action.launchMode()) {
                                LaunchMode.Standard -> {
                                    //doNothing
                                    LogUtil.log("enter this line Standard")
                                }
                                LaunchMode.SingleTop -> {
                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                }
                                LaunchMode.SingleTask -> {
                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                LaunchMode.NewTask -> {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                        }
                        putExtras(bundle)
                        actualContext.startActivity(intent)
                        return DirectRequestResult.ActivityResult(bundle, contextHolder)
                    }
                }
                return DirectRequestResult.Empty(bundle, contextHolder)
            }
            is Target.FragmentTarget -> {
                checkThread(target.action.thread)
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz, bundle)
                target.action.inject(bundle, result)
                return DirectRequestResult.FragmentResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.InterceptorTarget -> {
                checkThread(target.action.thread)
                val result =
                    target.action.factory().create(contextHolder, target.targetClazz)
                target.action.inject(bundle, result)
                return DirectRequestResult.InterceptorResult(
                    result,
                    target.action,
                    bundle,
                    contextHolder
                )
            }
            is Target.SystemTarget -> {
                val actualUri = ParameterSupport.getUriAsString(bundle)
                val actualContext = contextHolder.getContext()
                var intent = Intent.parseUri(actualUri, 0)
                var info = actualContext.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                if (info != null) {
                    if (info.activityInfo.packageName != actualContext.packageName) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    actualContext.startActivity(intent)
                    return DirectRequestResult.ActivityResult(bundle, contextHolder)
                }
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(actualUri)
                intent.putExtras(bundle)
                info = actualContext.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                with(info) {
                    return if (this == null) {
                        LogUtil.log(
                            "没找到对应路径{'${
                                actualUri
                            }'}的组件,请检查路径以及拦截器的设置"
                        )
                        DirectRequestResult.Empty(bundle, contextHolder)
                    } else {
                        if (activityInfo.packageName != actualContext.packageName) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        actualContext.startActivity(intent)
                        DirectRequestResult.ActivityResult(bundle, contextHolder)
                    }
                }
            }
        }
    }
}