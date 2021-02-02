package com.x930073498.component.router.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private const val FLAG_BOOLEAN: Boolean = false
private const val FLAG_INT: Int = 0
private const val FLAG_LONG: Long = 1L
private const val FLAG_DOUBLE: Double = 0.0
private const val FLAG_FLOAT: Float = 1f
private const val FLAG_CHAR: Char = '0'
private const val FLAG_SHORT: Short = 0
private const val FLAG_BYTE: Byte = 0

inline fun <reified T> getTypeToken() = object : TypeToken<T>() {}
inline fun <reified T> T.getTypeToken() = object : TypeToken<T>() {}
inline fun <reified T> T.getType() = getTypeToken().getType()
inline fun <reified T> getType() = getTypeToken<T>().getType()

fun Type.asTypeToken() = TypeToken.get(this)

fun TypeToken<*>.isBoolean() = rawType.isInstance(FLAG_BOOLEAN)

fun TypeToken<*>.isInt() = rawType.isInstance(FLAG_INT)

fun TypeToken<*>.isDouble() = rawType.isInstance(FLAG_DOUBLE)

fun TypeToken<*>.isLong() = rawType.isInstance(FLAG_LONG)

fun TypeToken<*>.isFloat() = rawType.isInstance(FLAG_FLOAT)

fun TypeToken<*>.isChar() = rawType.isInstance(FLAG_CHAR)

fun TypeToken<*>.isShort() = rawType.isInstance(FLAG_SHORT)

fun TypeToken<*>.isByte() = rawType.isInstance(FLAG_BYTE)


inline fun <reified T> TypeToken<*>.isAssignableFrom() = isAssignableFrom(getType<T>())

inline fun <reified T> TypeToken<*>.isAssignableTo() = getTypeToken<T>().isAssignableFrom(this)


fun TypeToken<*>.isArrayOf(type: Type) = isAssignableFrom(TypeToken.getArray(type))

inline fun <reified T> TypeToken<*>.isArrayOf() = isArrayOf(getType<T>())

fun TypeToken<*>.isListOf(type: Type): Boolean {
    if (!List::class.java.isAssignableFrom(rawType)) return false
    val componentType = Types.getCollectionElementType(getType(), List::class.java)
    return type.isAssignableFrom(componentType)
}

inline fun <reified T> TypeToken<*>.isListOf(): Boolean {
    return isListOf(getType<T>())
}

fun TypeToken<*>.isArrayListOf(type: Type): Boolean {
    if (!ArrayList::class.java.isAssignableFrom(rawType)) return false
    val componentType = Types.getCollectionElementType(getType(), ArrayList::class.java)
    return type.isAssignableFrom(componentType)
}

inline fun <reified T> TypeToken<*>.isArrayListOf(): Boolean {
    return isArrayListOf(getType<T>())
}

fun Type.isBoolean() = asTypeToken().isBoolean()

fun Type.isInt() = asTypeToken().isInt()

fun Type.isDouble() = asTypeToken().isDouble()

fun Type.isLong() = asTypeToken().isLong()

fun Type.isFloat() = asTypeToken().isFloat()

fun Type.isChar() = asTypeToken().isChar()

fun Type.isShort() = asTypeToken().isShort()

fun Type.isByte() = asTypeToken().isByte()

inline fun <reified T> Type.isAssignableFrom() = asTypeToken().isAssignableFrom<T>()

inline fun <reified T> Type.isAssignableTo() = getTypeToken<T>().isAssignableFrom(this)

fun Type.isAssignableFrom(type: Type) = asTypeToken().isAssignableFrom(type)

fun Type.isAssignableTo(type: Type) =type.asTypeToken().isAssignableFrom(this)


fun Type.isArrayOf(type: Type) = asTypeToken().isArrayOf(type)
inline fun <reified T> Type.isArrayOf() = asTypeToken().isArrayOf<T>()

fun Type.isListOf(type: Type) = asTypeToken().isListOf(type)
inline fun <reified T> Type.isListOf() = asTypeToken().isListOf<T>()


fun Type.isArrayListOf(type: Type) = asTypeToken().isArrayListOf(type)
inline fun <reified T> Type.isArrayListOf() = asTypeToken().isArrayListOf<T>()


fun parameterizedTypeOf(type: Type, vararg argumentType: Type): ParameterizedType {
    return Types.newParameterizedTypeWithOwner(null, type, *argumentType)
}


