package com.x930073498.util

import com.squareup.kotlinpoet.*
import com.x930073498.annotations.ServiceAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.bean.RouterInfo
import com.x930073498.compiler.BaseProcessor
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror


fun buildTargetFunction(
    targetClassName: ClassName,
    element: TypeElement,
    typeSpec: TypeSpec.Builder,
) {
    val target = FunSpec.builder("target")
        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
        .addStatement(
            "return %T(%T::class.java)",
            targetClassName,
            element.asClassName()
        )
    typeSpec.addFunction(target.build())
}

fun buildToStringFunction(
    element: TypeElement,
    typeSpec: TypeSpec.Builder,
) {
    val target = FunSpec.builder("toString")
        .addModifiers(KModifier.OVERRIDE)
        .addStatement(
            "return \"path=\$path,group=\$group,targetClass=\${%T::class.java}\"",
            element.asClassName()
        )
    typeSpec.addFunction(target.build())
}

fun buildTargetFunction(
    fragmentTargetClassName: ClassName,
    element: TypeElement,
    serviceAnnotation: ServiceAnnotation,
    typeSpec: TypeSpec.Builder,
) {
    val target = FunSpec.builder("target")
        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
        .addStatement(
            "return %T(%T::class.java,%L)",
            fragmentTargetClassName,
            element.asClassName(),
            serviceAnnotation.singleton
        )
    typeSpec.addFunction(target.build())
}

fun BaseProcessor.buildKeyProperty(
    typeSpec: TypeSpec.Builder,
    info: RouterInfo,
) {

    val key = PropertySpec.builder("path", String::class)
        .addModifiers(KModifier.OVERRIDE)
        .mutable(false)
        .initializer("%S", info.path)
    val authorityProperty = PropertySpec.builder("group", String::class)
        .addModifiers(KModifier.OVERRIDE)
        .mutable(false)
        .initializer("%S", info.group)
    typeSpec.addProperty(key.build())
    typeSpec.addProperty(authorityProperty.build())
}

fun getGroupFromPath(path: String?): String? {
    val group = runCatching { path?.substring(1, path.indexOf("/", 1)) }.onFailure {
//        messager.printMessage(Diagnostic.Kind.ERROR,
//            "Failed to extract default group! " + it.message)
    }.getOrNull() ?: return null
    if (group.isEmpty()) return null
    return group
}

fun BaseProcessor.generateParameterCodeForInject(
    variableElement: VariableElement,
    methodBuilder: FunSpec.Builder,
    parameterName: String,
    bundleCallStr: String,
    fragmentCallStr: String,
) {
    val annotation: ValueAutowiredAnnotation =
        variableElement.getAnnotation(ValueAutowiredAnnotation::class.java)
    val methodName = getParameterMethodName(variableElement)
    if (methodName.isNotEmpty())
        methodBuilder.addStatement(
            "%L.%N = %T.%L(%N,%S)?:%L.%N",
            fragmentCallStr,
            parameterName,
            parameterSupportTypeMirror,
            methodName,
            bundleCallStr,
            annotation.name.ifEmpty { parameterName },
            fragmentCallStr,
            parameterName,
        )

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