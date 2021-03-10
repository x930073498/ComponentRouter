package com.x930073498.component.router.compiler

import com.squareup.kotlinpoet.*
import com.x930073498.component.annotations.*
import com.x930073498.component.router.compiler.ActivityInfo
import com.x930073498.component.router.compiler.BaseProcessor
import com.x930073498.component.router.compiler.FragmentInfo
import com.x930073498.component.router.compiler.Generator
import com.x930073498.component.router.compiler.InterceptorInfo
import com.x930073498.component.router.compiler.MethodInfo
import com.x930073498.component.router.compiler.MethodInvokerInfo
import com.x930073498.component.router.compiler.ParameterInfo
import com.x930073498.component.router.compiler.ServiceInfo
import com.x930073498.component.router.compiler.TypeInfo
import com.x930073498.component.router.compiler.ValueAutowired
import com.x930073498.component.router.data.Params
import com.x930073498.component.router.data.RouterDoc
import com.x930073498.component.router.util.*
import com.x930073498.component.router.util.ServiceConstants.SERVICE_NAME
import org.jetbrains.annotations.Nullable
import java.util.*
import javax.lang.model.element.*
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.coroutines.Continuation

class RouterCompilerException(msg: String) : Exception(msg)

private var _emptyInfo: TypeInfo? = null

val BaseProcessor.emptyTypeInfo: TypeInfo
    get() {
        return _emptyInfo.let {
            it ?: TypeInfo.Empty(this).apply {
                _emptyInfo = this
            }
        }
    }


internal fun BaseProcessor.getInfo(element: Element): Generator {
    var info = getFragmentInfo(element)
    if (info != emptyTypeInfo) return info.getGenerator()
    info = getActivityInfo(element)
    if (info != emptyTypeInfo) return info.getGenerator()
    info = getServiceInfo(element)
    if (info != emptyTypeInfo) return info.getGenerator()
    info = getInterceptorInfo(element)
    if (info != emptyTypeInfo) return info.getGenerator()
    return getMethodInfo(element).getGenerator()

}

internal fun BaseProcessor.generate(element: Element) {
    getInfo(element).generate()
}

internal fun BaseProcessor.getInterceptorInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation =
        element.getAnnotation(InterceptorAnnotation::class.java) ?: return emptyTypeInfo
    val interceptorType =
        elements.getTypeElement(InterceptorConstants.INTERCEPTOR_NAME.canonicalName).asType()
    if (!types.isSubtype(element.asType(), interceptorType)) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$element is Not RouterInterceptor")
        throw RouterCompilerException("$element is Not RouterInterceptor")
    }
    val group: String = annotation.realGroup()
    val path: String = annotation.realPath()

    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}InterceptorActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val typeName = element.asType().asTypeName()
    return InterceptorInfo(
        this,
        annotation,
        path,
        group,
        classPrefixName,
        className,
        packageName,
        typeName,
        element = element,
        factoryTypeName = InterceptorConstants.INTERCEPTOR_ACTION_DELEGATE_FACTORY_NAME,
        injectTargetTypeName = null,
        superClassName = AUTO_ACTION_NAME,
        interceptors = arrayOf(),
        supperInterfaces = arrayListOf(
            InterceptorConstants.INTERCEPTOR_ACTION_DELEGATE_NAME,
            I_AUTO_NAME
        ),
        parentPath = ""
    ).apply { collectDoc() }

}

private fun <T> BaseProcessor.findParentAnnotation(
    type: TypeMirror,
    boundType: TypeMirror,
    clazz: Class<T>,
    path: String = ""
): T? where T : Annotation {
    var current: TypeMirror = type
    while (types.isSubtype(current, boundType)) {
        val temp = types.directSupertypes(current).firstOrNull {
            types.asElement(it).kind == ElementKind.CLASS
        }
        if (temp != null) {
            val annotation = types.asElement(temp).getAnnotation(clazz)
            if (annotation != null) return annotation
            else current = temp
        } else {
            return null
        }
    }
    return null
}

internal fun BaseProcessor.getFragmentInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation = element.getAnnotation(FragmentAnnotation::class.java) ?: return emptyTypeInfo
    if (!types.isSubtype(
            element.asType(),
            fragmentTypeMirror
        )
    ) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$element is Not Fragment")
        throw RouterCompilerException("$element is Not Fragment")
    }
    val interceptors = annotation.interceptors

    val group: String = annotation.realGroup()
    val path: String = annotation.realPath()

    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}FragmentActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType();
    val typeName = type.asTypeName()

    val parentAnnotation =
        findParentAnnotation(type, fragmentTypeMirror, FragmentAnnotation::class.java, path)
    return FragmentInfo(
        this,
        annotation,
        path,
        group,
        classPrefixName,
        className,
        packageName,
        typeName,
        element = element,
        factoryTypeName = FragmentConstants.FRAGMENT_ACTION_DELEGATE_FACTORY_NAME,
        injectTargetTypeName = FragmentConstants.FRAGMENT_NAME,
        superClassName = AUTO_ACTION_NAME,
        interceptors = interceptors,
        parentPath = parentAnnotation?.path ?: "",
        supperInterfaces = arrayListOf(
            FragmentConstants.FRAGMENT_ACTION_DELEGATE_NAME,
            I_AUTO_NAME
        )
    ).apply {
        autoInjectList.addAll(element.enclosedElements.mapNotNull {
            val injectAnnotation = it.getAnnotation(ValueAutowiredAnnotation::class.java)
                ?: return@mapNotNull null
            val elementName = it.simpleName.toString()
            val name = injectAnnotation.name.ifEmpty { elementName }
            if (it is VariableElement) {
                return@mapNotNull ValueAutowired(it, injectAnnotation, name, elementName, this)
            } else {
                return@mapNotNull null
            }
        })
    }.apply {
        collectDoc()
    }
}

internal fun BaseProcessor.getServiceInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation = element.getAnnotation(ServiceAnnotation::class.java) ?: return emptyTypeInfo
    val serviceTypeMirror = elements.getTypeElement(SERVICE_NAME.canonicalName).asType()
    if (!types.isSubtype(
            element.asType(),
            serviceTypeMirror
        )
    ) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$element is Not IService")
        throw RouterCompilerException("$element is Not IService")
    }
    val group: String = annotation.realGroup()
    val path: String = annotation.realPath()
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}ServiceActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType()
    val typeName = type.asTypeName()
    val parentAnnotation =
        findParentAnnotation(type, serviceTypeMirror, ServiceAnnotation::class.java, path)
    return ServiceInfo(
        this,
        annotation,
        path,
        group,
        classPrefixName,
        className,
        packageName,
        typeName,
        element = element,
        isAutoInvoke = annotation.autoInvoke,
        isSingleTone = annotation.singleton,
        factoryTypeName = ServiceConstants.SERVICE_ACTION_DELEGATE_FACTORY_NAME,
        injectTargetTypeName = SERVICE_NAME,
        interceptors = annotation.interceptors,
        superClassName = AUTO_ACTION_NAME,
        parentPath = parentAnnotation?.path ?: "",
        supperInterfaces = arrayListOf(
            ServiceConstants.SERVICE_ACTION_DELEGATE_NAME,
            I_AUTO_NAME
        )
    ).apply {
        autoInjectList.addAll(element.enclosedElements.mapNotNull {
            val injectAnnotation = it.getAnnotation(ValueAutowiredAnnotation::class.java)
                ?: return@mapNotNull null
            val elementName = it.simpleName.toString()
            val name = injectAnnotation.name.ifEmpty { elementName }
            if (it is VariableElement) {
                return@mapNotNull ValueAutowired(it, injectAnnotation, name, elementName, this)
            } else {
                return@mapNotNull null
            }
        })
    }.apply {
        collectDoc()
    }
}

internal fun BaseProcessor.getActivityInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation = element.getAnnotation(ActivityAnnotation::class.java) ?: return emptyTypeInfo
    if (!types.isSubtype(
            element.asType(),
            activityTypeMirror
        )
    ) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$element is Not Activity")
        throw RouterCompilerException("$element is Not Activity")
    }
    val group: String = annotation.realGroup()
    val path: String = annotation.realPath()
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}ActivityActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType()
    val typeName = type.asTypeName()
    val parentAnnotation =
        findParentAnnotation(type, activityTypeMirror, ActivityAnnotation::class.java)
    return ActivityInfo(
        this,
        annotation,
        path,
        group,
        classPrefixName,
        className,
        packageName,
        typeName,
        element = element,
        injectTargetTypeName = ActivityConstants.ACTIVITY_NAME,
        superClassName = AUTO_ACTION_NAME,
        interceptors = annotation.interceptors,
        parentPath = parentAnnotation?.path ?: "",
        supperInterfaces = arrayListOf(
            ActivityConstants.ACTIVITY_ACTION_DELEGATE_NAME,
            I_AUTO_NAME
        )
    ).apply {
        autoInjectList.addAll(element.enclosedElements.mapNotNull {
            val injectAnnotation = it.getAnnotation(ValueAutowiredAnnotation::class.java)
                ?: return@mapNotNull null
            val elementName = it.simpleName.toString()
            val name = injectAnnotation.name.ifEmpty { elementName }
            if (it is VariableElement) {
                return@mapNotNull ValueAutowired(it, injectAnnotation, name, elementName, this)
            } else {
                return@mapNotNull null
            }
        })
    }.apply {
        collectDoc()
    }
}

internal fun BaseProcessor.getMethodInfo(element: Element): MethodInfo {
    val annotation =
        element.getAnnotation(MethodAnnotation::class.java) ?: return MethodInfo(emptyTypeInfo)
    if (element.kind != ElementKind.METHOD) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$element is Not a method")
        throw RouterCompilerException("$element is Not a method")
    }
    element as ExecutableElement
    val group: String = annotation.realGroup()
    val path: String = annotation.realPath()

    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}MethodActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val methodInvokerInfo = MethodInvokerInfo(
        this,
        annotation,
        path,
        group,
        classPrefixName,
        className,
        packageName,
        ANY,
        element = element,
        factoryTypeName = MethodConstants.METHOD_ACTION_DELEGATE_FACTORY_NAME,
        injectTargetTypeName = null,
        superClassName = AUTO_ACTION_NAME,
        interceptors = annotation.interceptors,
        parentPath = "",
        supperInterfaces = arrayListOf(
            MethodConstants.METHOD_ACTION_DELEGATE_NAME,
            I_AUTO_NAME
        )
    )
    val parameters = element.parameters
    val list = parameters.mapIndexedNotNull { index, it ->
        val type = it.asType()
        val typeName = type.asTypeName().javaToKotlinType()
        val isNullable = it.getAnnotation(Nullable::class.java) != null
        if (typeName is ParameterizedTypeName && typeName.rawType == Continuation::class.asTypeName() && index == parameters.size - 1) {
            return@mapIndexedNotNull null
        }
        val methodBundleNameAnnotation =
            it.getAnnotation(MethodBundleNameAnnotation::class.java)
        val parameterMethodName = getParameterMethodName(it)
        val isContext = types.isSubtype(type, contextTypeMirror)
        ParameterInfo(
            methodBundleNameAnnotation?.name ?: it.simpleName.toString(),
            methodBundleNameAnnotation?.desc ?: "",
            parameterMethodName,
            typeName,
            isNullable,
            isContext
        )
    }
    return MethodInfo(
        methodInvokerInfo, MemberName(packageName, element.simpleName.toString()),
        list
    ).apply {
        if (isDocEnable) {
            docList.add(
                RouterDoc(
                    group,
                    path,
                    methodMemberName.toString(),
                    "method",
                    annotation.desc,
                    list.filter { !it.isContext }.map {
                        Params(
                            it.name,
                            it.typeName.toString(),
                            it.desc,
                            !it.nullable && !it.isContext
                        )
                    })
            )
        }
    }
}


internal fun pathCapitalize(path: String): String {
    return path.split("/").fold(StringBuilder()) { builder, it ->
        builder.append(it.capitalize(Locale.getDefault()))
    }.toString()
}


fun BaseProcessor.getParameterMethodName(variableElement: VariableElement): String {
    val variableTypeMirror = variableElement.asType()
    return getParameterMethodName(variableElement, variableTypeMirror)
}

fun BaseProcessor.getParameterMethodName(
    variableElement: VariableElement,
    variableTypeMirror: TypeMirror?
): String {
    val parameterClassName: TypeName = variableElement.javaToKotlinType()
    val isSubParcelableType: Boolean = types.isSubtype(variableTypeMirror, parcelableTypeMirror)

    val isSubSerializableType: Boolean =
        types.isSubtype(variableTypeMirror, serializableTypeMirror)
    return getParameterMethodName(
        parameterClassName,
        variableTypeMirror,
        isSubParcelableType,
        isSubSerializableType
    )
}

private fun BaseProcessor.getParameterMethodName(
    parameterClassName: TypeName,
    variableTypeMirror: TypeMirror?,
    isSubParcelableType: Boolean,
    isSubSerializableType: Boolean
): String {
    return "get"
//    getMethodName(
//        parameterClassName,
//        variableTypeMirror,
//        isSubParcelableType,
//        isSubSerializableType
//    )
}


