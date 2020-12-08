package com.x930073498.util

import com.squareup.kotlinpoet.*
import com.x930073498.annotations.*
import com.x930073498.compiler.BaseProcessor
import org.jetbrains.annotations.Nullable
import java.lang.StringBuilder
import java.util.*
import javax.lang.model.element.*
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.coroutines.Continuation

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
    val annotationGroup = annotation.group
    val annotationPath = annotation.path
    val group: String
    val path: String
    if (annotationGroup.isEmpty()) {
        group = getGroupFromPath(annotationPath) ?: ""
        path = annotationPath
    } else {
        group = annotationGroup
        path = "/${group}$annotationPath"
    }
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}InterceptorActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val typeName = element.asType().asTypeName()
    return InterceptorInfo(
        this,
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
    )

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
    ) return emptyTypeInfo
    val interceptors = annotation.interceptors
    val annotationGroup = annotation.group
    val annotationPath = annotation.path
    val group: String
    val path: String
    if (annotationGroup.isEmpty()) {
        group = getGroupFromPath(annotationPath) ?: ""
        path = annotationPath
    } else {
        group = annotationGroup
        path = "/${group}$annotationPath"
    }
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}FragmentActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType();
    val typeName = type.asTypeName()

    val parentAnnotation =
        findParentAnnotation(type, fragmentTypeMirror, FragmentAnnotation::class.java, path)
    messager.printMessage(Diagnostic.Kind.NOTE, "path=$path,parentAnnotation=$parentAnnotation")
    return FragmentInfo(
        this,
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
        supperInterfaces = arrayListOf(FragmentConstants.FRAGMENT_ACTION_DELEGATE_NAME, I_AUTO_NAME)
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
    }
}

internal fun BaseProcessor.getServiceInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation = element.getAnnotation(ServiceAnnotation::class.java) ?: return emptyTypeInfo
    val annotationGroup = annotation.group
    val annotationPath = annotation.path
    val group: String
    val path: String
    if (annotationGroup.isEmpty()) {
        group = getGroupFromPath(annotationPath) ?: ""
        path = annotationPath
    } else {
        group = annotationGroup
        path = "/${group}$annotationPath"
    }
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}ServiceActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType()
    val typeName = type.asTypeName()
    val parentAnnotation = findParentAnnotation(type, fragmentTypeMirror, ServiceAnnotation::class.java, path)
    return ServiceInfo(
        this,
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
        injectTargetTypeName = ServiceConstants.SERVICE_NAME,
        interceptors = annotation.interceptors,
        superClassName = AUTO_ACTION_NAME,
        parentPath = parentAnnotation?.path ?: "",
        supperInterfaces = arrayListOf(ServiceConstants.SERVICE_ACTION_DELEGATE_NAME, I_AUTO_NAME)
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
    }
}

internal fun BaseProcessor.getActivityInfo(element: Element): TypeInfo {
    if (element.kind != ElementKind.CLASS) return emptyTypeInfo
    val annotation = element.getAnnotation(ActivityAnnotation::class.java) ?: return emptyTypeInfo
    val annotationGroup = annotation.group
    val annotationPath = annotation.path
    val group: String
    val path: String
    if (annotationGroup.isEmpty()) {
        group = getGroupFromPath(annotationPath) ?: ""
        path = annotationPath
    } else {
        group = annotationGroup
        path = "/${group}$annotationPath"
    }
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}ActivityActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val type = element.asType()
    val typeName = type.asTypeName()
    val parentAnnotation =
        findParentAnnotation(type, activityTypeMirror, ActivityAnnotation::class.java)
    messager.printMessage(Diagnostic.Kind.NOTE, "path=$path,parentAnnotation=$parentAnnotation")
    return ActivityInfo(
        this,
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
        supperInterfaces = arrayListOf(ActivityConstants.ACTIVITY_ACTION_DELEGATE_NAME, I_AUTO_NAME)
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
    }
}

internal fun BaseProcessor.getMethodInfo(element: Element): MethodInfo {
    if (element.kind != ElementKind.METHOD) return MethodInfo(emptyTypeInfo)
    element as ExecutableElement
    val annotation =
        element.getAnnotation(MethodAnnotation::class.java) ?: return MethodInfo(emptyTypeInfo)
    val annotationGroup = annotation.group
    val annotationPath = annotation.path
    val group: String
    val path: String
    if (annotationGroup.isEmpty()) {
        group = getGroupFromPath(annotationPath) ?: ""
        path = annotationPath
    } else {
        group = annotationGroup
        path = "/${group}$annotationPath"
    }
    val classPrefixName = "_${pathCapitalize(path)}"
    val className = "${classPrefixName}MethodActionDelegate"
    val packageName = elements.getPackageOf(element).qualifiedName.toString()
    val methodInvokerInfo = MethodInvokerInfo(
        this,
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
        supperInterfaces = arrayListOf(MethodConstants.METHOD_ACTION_DELEGATE_NAME, I_AUTO_NAME)
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
            parameterMethodName,
            typeName,
            isNullable,
            isContext
        )
    }
    return MethodInfo(
        methodInvokerInfo, MemberName(packageName, element.simpleName.toString()),
        list
    )
}


internal fun pathCapitalize(path: String): String {
    return path.split("/").fold(StringBuilder()) { builder, it ->
        builder.append(it.capitalize(Locale.getDefault()))
    }.toString()
}

fun getGroupFromPath(path: String?): String? {
    val group = runCatching { path?.substring(1, path.indexOf("/", 1)) }.onFailure {
//        messager.printMessage(Diagnostic.Kind.ERROR,
//            "Failed to extract default group! " + it.message)
    }.getOrNull() ?: return null
    if (group.isEmpty()) return null
    return group
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
    val noNullParameterClassName = parameterClassName.copy(false)
    return if (noNullParameterClassName == mTypeNameString) { // 如果是一个 String
        "getString"
    } else if (noNullParameterClassName == charSequenceTypeName) { // 如果是一个 charsequence
        "getCharSequence"
    } else if (noNullParameterClassName == CHAR) { // 如果是一个 char or Char
        "getChar"
    } else if (noNullParameterClassName == BYTE) { // 如果是一个byte or Byte
        "getByte"
    } else if (noNullParameterClassName == SHORT) { // 如果是一个short or Short
        "getShort"
    } else if (noNullParameterClassName == INT) { // 如果是一个int or Integer
        "getInt"
    } else if (noNullParameterClassName == LONG) { // 如果是一个long or Long
        "getLong"
    } else if (noNullParameterClassName == FLOAT) { // 如果是一个float or Float
        "getFloat"
    } else if (noNullParameterClassName == DOUBLE) { // 如果是一个double or Double
        "getDouble"
    } else if (noNullParameterClassName == BOOLEAN) { // 如果是一个boolean or Boolean
        "getBoolean"
    } else if (variableTypeMirror is DeclaredType) {
        if (variableTypeMirror.asElement() is TypeElement) {
            val className: TypeName =
                (variableTypeMirror.asElement() as TypeElement).javaToKotlinType()
            if (arrayListClassName == className) { // 如果外面是 ArrayList
                val typeArguments = variableTypeMirror.typeArguments
                if (typeArguments.size == 1) { // 处理泛型个数是一个的
                    when {
                        types.isSubtype(
                            typeArguments[0],
                            parcelableTypeMirror
                        ) -> { // 如果是 Parcelable 及其子类
                            "getParcelableArrayList"
                        }
                        types.isSubtype(
                            typeArguments[0],
                            serializableTypeMirror
                        ) -> { // 如果是 Serializable 及其子类
                            "getSerializable"
                        }
                        mTypeElementString.javaToKotlinType() == typeArguments[0].asTypeName()
                            .javaToKotlinType() -> {
                            "getStringArrayList"
                        }
                        charSequenceTypeMirror.asTypeName()
                            .javaToKotlinType() == typeArguments[0].asTypeName()
                            .javaToKotlinType() -> {
                            "getCharSequenceArrayList"
                        }
                        mTypeElementInteger.asClassName()
                            .javaToKotlinType() == typeArguments[0].asTypeName()
                            .javaToKotlinType() -> {
                            "getIntegerArrayList"
                        }
                        else -> {
                            ""
                        }
                    }
                } else {
                    ""
                }
            } else if (mTypeNameSparseArray == className) { // 如果是 SparseArray
                val typeArguments = variableTypeMirror.typeArguments
                if (typeArguments.size == 1) { // 处理泛型个数是一个的
                    if (types.isSubtype(
                            typeArguments[0],
                            parcelableTypeMirror
                        )
                    ) { // 如果是 Parcelable 及其子类
                        "getSparseParcelableArray"
                    } else {
                        ""
                    }
                } else { // 其他类型的情况,是实现序列化的对象,这种时候我们直接要从 bundle 中获取

                    // 优先获取 parcelable
                    when {
                        isSubParcelableType -> {
                            "getParcelable"
                        }
                        isSubSerializableType -> {
                            "getSerializable"
                        }
                        else -> {
                            ""
                        }
                    }
                }
            } else if (isSubParcelableType) {
                "getParcelable"
            } else if (isSubSerializableType) {
                "getSerializable"
            } else {
                ""
            }
        } else if (variableTypeMirror is ArrayType) { // 如果是数组
            val parameterComponentTypeName: TypeName =
                variableTypeMirror.componentType.asTypeName().javaToKotlinType()
            // 如果是一个 String[]
            when {
                variableTypeMirror.componentType.asTypeName()
                    .javaToKotlinType() == mTypeNameString -> {
                    "getStringArray"
                }
                variableTypeMirror.componentType.asTypeName()
                    .javaToKotlinType() == charSequenceTypeName -> {
                    "getCharSequenceArray"
                }
                variableTypeMirror.componentType.asTypeName()
                    .javaToKotlinType() == mTypeNameString -> {
                    "getStringArray"
                }
                parameterComponentTypeName == BYTE -> { // 如果是 byte
                    "getByteArray"
                }
                parameterComponentTypeName == CHAR -> { // 如果是 char
                    "getCharArray"
                }
                parameterComponentTypeName == SHORT -> { // 如果是 short
                    "getShortArray"
                }
                parameterComponentTypeName == INT -> { // 如果是 int
                    "getIntArray"
                }
                parameterComponentTypeName == LONG -> { // 如果是 long
                    "getLongArray"
                }
                parameterComponentTypeName == FLOAT -> { // 如果是 float
                    "getFloatArray"
                }
                parameterComponentTypeName == DOUBLE -> { // 如果是 double
                    "getDoubleArray"
                }
                parameterComponentTypeName == BOOLEAN -> { // 如果是 boolean
                    "getBooleanArray"
                }
                types.isSameType(
                    variableTypeMirror.componentType,
                    parcelableTypeMirror
                ) -> {  // 如果是 Parcelable
                    "getParcelableArray"
                }
                else -> {
                    ""
                }
            }
        } else if (isSubParcelableType) {
            "getParcelable"
        } else if (isSubSerializableType) {
            "getSerializable"
        } else {
            ""
        }

    } else if (isSubParcelableType) {
        "getParcelable"
    } else if (isSubSerializableType) {
        "getSerializable"
    } else {
        ""
    }
}