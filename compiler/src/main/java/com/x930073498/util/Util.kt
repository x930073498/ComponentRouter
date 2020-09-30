package com.x930073498.util

import com.squareup.kotlinpoet.*
import com.x930073498.annotations.ServiceAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.compiler.BaseProcessor
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType


 fun buildTargetFunction(
     targetClassName: ClassName,
     element: TypeElement,
     typeSpec: TypeSpec.Builder,
) {
    val target = FunSpec.builder("target")
        .addModifiers(KModifier.OVERRIDE,KModifier.SUSPEND)
        .addStatement("return %T(%T::class.java)",
            targetClassName,
            element.asClassName())
    typeSpec.addFunction(target.build())
}
 fun buildTargetFunction(
     fragmentTargetClassName: ClassName,
     element: TypeElement,
     serviceAnnotation: ServiceAnnotation,
     typeSpec: TypeSpec.Builder,
) {
    val target = FunSpec.builder("target")
        .addModifiers(KModifier.OVERRIDE,KModifier.SUSPEND)
        .addStatement("return %T(%T::class.java,%L)",
            fragmentTargetClassName,
            element.asClassName(),
            serviceAnnotation.singleton
        )
    typeSpec.addFunction(target.build())
}

 fun buildKeyProperty(
    typeSpec: TypeSpec.Builder,
    authority:String?,
    path:String,
) {
    val keyString = Uri.Builder()
        .authority(authority?.run { if (isNotEmpty()) this else null })
        .path(path)
        .build().toSafeString()
    val key = PropertySpec.builder("key", String::class)
        .addModifiers(KModifier.OVERRIDE)
        .mutable(false)
        .initializer("%S", keyString)
    typeSpec.addProperty(key.build())
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
    val variableTypeMirror = variableElement.asType()
    val parameterClassName: TypeName = variableElement.asType().asTypeName()
    val isSubParcelableType: Boolean = types.isSubtype(variableTypeMirror, parcelableTypeMirror)

    val isSubSerializableType: Boolean =
        types.isSubtype(variableTypeMirror, serializableTypeMirror)
    val methodName = if (parameterClassName == mClassNameString) { // 如果是一个 String
        "getString"
    } else if (parameterClassName == charSequenceTypeName) { // 如果是一个 charsequence
        "getCharSequence"
    } else if (parameterClassName == CHAR) { // 如果是一个 char or Char
        "getChar"
    } else if (parameterClassName == BYTE) { // 如果是一个byte or Byte
        "getByte"
    } else if (parameterClassName == SHORT) { // 如果是一个short or Short
        "getShort"
    } else if (parameterClassName == INT) { // 如果是一个int or Integer
        "getInt"
    } else if (parameterClassName == LONG) { // 如果是一个long or Long
        "getLong"
    } else if (parameterClassName == FLOAT) { // 如果是一个float or Float
        "getFloat"
    } else if (parameterClassName == DOUBLE) { // 如果是一个double or Double
        "getDouble"
    } else if (parameterClassName == BOOLEAN) { // 如果是一个boolean or Boolean
        "getBoolean"
    } else if (variableTypeMirror is DeclaredType) {
        if (variableTypeMirror.asElement() is TypeElement) {
            val typeElement = variableTypeMirror.asElement() as TypeElement
            val className: ClassName = typeElement.asClassName()
            if (arrayListClassName == className) { // 如果外面是 ArrayList
                val typeArguments = variableTypeMirror.typeArguments
                if (typeArguments.size == 1) { // 处理泛型个数是一个的
                    when {
                        types.isSubtype(typeArguments[0],
                            parcelableTypeMirror) -> { // 如果是 Parcelable 及其子类
                            "getParcelableArrayList"
                        }
                        types.isSubtype(typeArguments[0],
                            serializableTypeMirror) -> { // 如果是 Serializable 及其子类
                            "getSerializable"
                        }
                        mTypeElementString.asType() == typeArguments[0] -> {
                            "getStringArrayList"
                        }
                        charSequenceTypeMirror == typeArguments[0] -> {
                            "getCharSequenceArrayList"
                        }
                        mTypeElementInteger.asType().equals(typeArguments[0]) -> {
                            "getIntegerArrayList"
                        }
                        else -> {
                            ""
                        }
                    }
                } else {
                    ""
                }
            } else if (mClassNameSparseArray == className) { // 如果是 SparseArray
                val typeArguments = variableTypeMirror.typeArguments
                if (typeArguments.size == 1) { // 处理泛型个数是一个的
                    if (types.isSubtype(typeArguments[0],
                            parcelableTypeMirror)
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
                variableTypeMirror.componentType.asTypeName()
            // 如果是一个 String[]
            when {
                variableTypeMirror.componentType == mTypeElementString.asType() -> {
                    "getStringArray"
                }
                variableTypeMirror.componentType == charSequenceTypeElement.asType() -> {
                    "getCharSequenceArray"
                }
                variableTypeMirror.componentType == mTypeElementString.asType() -> {
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
                types.isSameType(variableTypeMirror.componentType,
                    parcelableTypeMirror) -> {  // 如果是 Parcelable
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