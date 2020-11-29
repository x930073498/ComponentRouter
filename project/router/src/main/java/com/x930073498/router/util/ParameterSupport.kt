@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.x930073498.router.util

import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.SparseArray
import androidx.arch.core.util.Function
import java.io.Serializable
import java.util.*

/**
 * 传递参数是 Android 中的家常便事, 一般我们往 [Intent.putExtra] 各个方法中塞值. 我们称之为基础传值功能
 * 而 [ParameterSupport] 是在上述的 基础传值功能 上增加对 [Uri] 中的 [Uri.getQuery] 的支持
 * 举个例子：
 * <pre>
 * Router.with(this)
 * .url("router://xxx/xxx?name=xiaojinzi")
 * .putInt("age", 11)
 * .forward();
</pre> *
 * 上述代码中, 有两个参数：name 和 age
 * 如果你不通过 [ParameterSupport] 你获取不到 name 的值. 你只能获取到 age 的值
 * 而你通过 [ParameterSupport.getString] 就可以获取到 name 的值
 * 如果 [Uri] 的 query 中和 putXXX 方法的 key 相同呢？
 * <pre>
 * Router.with(this)
 * .url("router://xxx/xxx?name=xiaojinzi")
 * .putInt("name", "hello")
 * .forward();
</pre> *
 * 这时候你通过 [ParameterSupport.getString]
 * 根据 key = "name" 获取的话. 会得到 "xiaojinzi". 因为 query 的值的优先级比 Bundle 中的高
 * 如果 query 没有对应的值, 才会用 Bundle 中的, 比如下面的场景：
 * <pre>
 * Router.with(this)
 * .url("router://xxx/xxx?age=11")
 * .putInt("name", "hello")
 * .forward();
</pre> *
 * 这时候你通过 [ParameterSupport.getString]
 * 根据 key = "name" 获取的话. 会得到 "hello". 因为 query 中并没有 key = "name" 的值
 * 如果您想单独获取 query 中的值
 * [.getQueryBoolean] 您可以用 getQueryXXX 之类的方法单独获取 query 中的数据
 * time   : 2019/01/24
 *
 * @author : xiaojinzi
 */
object ParameterSupport {
    /**
     * 所有query的值都会被存在 bundle 中的这个 key 对应的内置 bundle 中
     * 也就是： bundle.bundle
     */
    const val KEY_URI_QUERY_BUNDLE = "_componentQueryBundle"
    const val KEY_URI = "_componentRouterUri"
    const val KEY_CENTER_KEY = "_routerCenterKey"
    fun syncUriToBundle(uri: Uri, bundle: Bundle) {
        val routerParameterBundle = Bundle()
        val queryParameterNames = uri.queryParameterNames
        if (queryParameterNames != null) {
            for (key in queryParameterNames) {
                val values = uri.getQueryParameters(key)
                routerParameterBundle.putStringArrayList(key, ArrayList(values))
            }
        }
        bundle.putBundle(KEY_URI_QUERY_BUNDLE, routerParameterBundle)
        bundle.putString(KEY_URI, uri.toString())
    }

    fun getUriIgnoreError(intent: Intent): Uri? {
        return try {
            val uriStr = getUriAsString(intent)
            if (uriStr == null) null else Uri.parse(uriStr)
        } catch (ignore: Exception) {
            null
        }
    }

    fun getUri(intent: Intent): Uri? {
        val uriStr = getUriAsString(intent)
        return if (uriStr == null) null else Uri.parse(uriStr)
    }

    fun getCenterKey(bundle: Bundle?): String? {
        return bundle?.getString(KEY_CENTER_KEY)
    }

    internal fun putCenter(bundle: Bundle?, key: String) {
        bundle?.putString(KEY_CENTER_KEY, key)
    }

    fun getCenterKey(bundle: Intent?): String? {
        return bundle?.getStringExtra(KEY_CENTER_KEY)
    }

    internal fun putCenter(bundle: Intent?, key: String) {
        bundle?.putExtra(KEY_CENTER_KEY, key)
    }

    fun getUriIgnoreError(bundle: Bundle): Uri? {
        return try {
            val uriStr = getUriAsString(bundle)
            if (uriStr == null) null else Uri.parse(uriStr)
        } catch (ignore: Exception) {
            null
        }
    }


    fun getUri(bundle: Bundle): Uri? {
        val uriStr = getUriAsString(bundle)
        return if (uriStr == null) null else Uri.parse(uriStr)
    }

    fun getUriAsString(bundle: Bundle): String? {
        return bundle.getString(KEY_URI)
    }

    fun getUriAsString(intent: Intent): String? {
        return if (intent.extras == null) {
            null
        } else {
            intent.extras!!.getString(KEY_URI)
        }
    }

    // ============================================================== 查询 query 的方法开始 ==============================================================
    fun <T> getQueries(bundle: Bundle?, key: String, function: Function<String, T>): List<T>? {
        if (bundle == null) {
            return null
        }
        val routerParameterBundle = bundle.getBundle(KEY_URI_QUERY_BUNDLE)
            ?: return null
        // may be null
        val values = routerParameterBundle.getStringArrayList(key)
        return if (values == null || values.isEmpty()) {
            null
        } else try {
            val result = ArrayList<T>(values.size)
            for (value in values) {
                result.add(function.apply(value))
            }
            result
        } catch (ignore: Exception) {
            null
        }
    }

    fun getQueryString(intent: Intent, key: String): String? {
        return getQueryString(intent, key, null)
    }

    fun getQueryString(intent: Intent, key: String, defaultValue: String?): String? {
        return getQueryString(intent.extras, key, defaultValue)
    }

    fun getQueryString(bundle: Bundle?, key: String): String? {
        return getQueryString(bundle, key, null)
    }

    fun getQueryString(bundle: Bundle?, key: String, defaultValue: String?): String? {
        val values = getQueryStrings(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryStrings(intent: Intent, key: String): List<String> {
        return getQueryStrings(intent.extras, key)!!
    }

    fun getQueryStrings(bundle: Bundle?, key: String): List<String>? {
        return getQueries(bundle, key) { s -> s }
    }

    fun getQueryInt(intent: Intent, key: String): Int? {
        return getQueryInt(intent, key, null)
    }

    fun getQueryInt(intent: Intent, key: String, defaultValue: Int?): Int? {
        return getQueryInt(intent.extras, key, defaultValue)
    }

    fun getQueryInt(bundle: Bundle?, key: String): Int? {
        return getQueryInt(bundle, key, null)
    }

    fun getQueryInt(bundle: Bundle?, key: String, defaultValue: Int?): Int? {
        val values = getQueryInts(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryInts(intent: Intent, key: String): List<Int> {
        return getQueryInts(intent.extras, key)!!
    }

    fun getQueryInts(bundle: Bundle?, key: String): List<Int>? {
        return getQueries(bundle, key) { s -> s.toInt() }
    }

    fun getQueryLong(intent: Intent, key: String): Long? {
        return getQueryLong(intent, key, null)
    }

    fun getQueryLong(intent: Intent, key: String, defaultValue: Long?): Long? {
        return getQueryLong(intent.extras, key, defaultValue)
    }

    fun getQueryLong(bundle: Bundle?, key: String): Long? {
        return getQueryLong(bundle, key, null)
    }

    fun getQueryLong(bundle: Bundle?, key: String, defaultValue: Long?): Long? {
        val values = getQueryLongs(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryLongs(intent: Intent, key: String): List<Long> {
        return getQueryLongs(intent.extras, key)!!
    }

    fun getQueryLongs(bundle: Bundle?, key: String): List<Long>? {
        return getQueries(bundle, key) { s -> s.toLong() }
    }

    fun getQueryDouble(intent: Intent, key: String): Double? {
        return getQueryDouble(intent, key, null)
    }

    fun getQueryDouble(intent: Intent, key: String, defaultValue: Double?): Double? {
        return getQueryDouble(intent.extras, key, defaultValue)
    }

    fun getQueryDouble(bundle: Bundle?, key: String): Double? {
        return getQueryDouble(bundle, key, null)
    }

    fun getQueryDouble(bundle: Bundle?, key: String, defaultValue: Double?): Double? {
        val values = getQueryDoubles(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryDoubles(intent: Intent, key: String): List<Double> {
        return getQueryDoubles(intent.extras, key)!!
    }

    fun getQueryDoubles(bundle: Bundle?, key: String): List<Double>? {
        return getQueries(bundle, key) { s -> s.toDouble() }
    }

    fun getQueryFloat(intent: Intent, key: String): Float? {
        return getQueryFloat(intent, key, null)
    }

    fun getQueryFloat(intent: Intent, key: String, defaultValue: Float?): Float? {
        return getQueryFloat(intent.extras, key, defaultValue)
    }

    fun getQueryFloat(bundle: Bundle?, key: String): Float? {
        return getQueryFloat(bundle, key, null)
    }

    fun getQueryFloat(bundle: Bundle?, key: String, defaultValue: Float?): Float? {
        val values = getQueryFloats(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryFloats(intent: Intent, key: String): List<Float> {
        return getQueryFloats(intent.extras, key)!!
    }

    fun getQueryFloats(bundle: Bundle?, key: String): List<Float>? {
        return getQueries(bundle, key) { s -> s.toFloat() }
    }

    fun getQueryBoolean(intent: Intent, key: String): Boolean? {
        return getQueryBoolean(intent, key, null)
    }

    fun getQueryBoolean(intent: Intent, key: String, defaultValue: Boolean?): Boolean? {
        return getQueryBoolean(intent.extras, key, defaultValue)
    }

    fun getQueryBoolean(bundle: Bundle?, key: String): Boolean? {
        return getQueryBoolean(bundle, key, null)
    }

    fun getQueryBoolean(bundle: Bundle?, key: String, defaultValue: Boolean?): Boolean? {
        val values = getQueryBooleans(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryBooleans(intent: Intent, key: String): List<Boolean> {
        return getQueryBooleans(intent.extras, key)!!
    }

    fun getQueryBooleans(bundle: Bundle?, key: String): List<Boolean>? {
        return getQueries(bundle, key) { s -> java.lang.Boolean.parseBoolean(s) }
    }

    fun getQueryShort(intent: Intent, key: String): Short? {
        return getQueryShort(intent, key, null)
    }

    fun getQueryShort(intent: Intent, key: String, defaultValue: Short?): Short? {
        return getQueryShort(intent.extras, key, defaultValue)
    }

    fun getQueryShort(bundle: Bundle?, key: String): Short? {
        return getQueryShort(bundle, key, null)
    }

    fun getQueryShort(bundle: Bundle?, key: String, defaultValue: Short?): Short? {
        val values = getQueryShorts(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryShorts(intent: Intent, key: String): List<Short> {
        return getQueryShorts(intent.extras, key)!!
    }

    fun getQueryShorts(bundle: Bundle?, key: String): List<Short>? {
        return getQueries(bundle, key) { s -> s.toShort() }
    }

    fun getQueryByte(intent: Intent, key: String): Byte? {
        return getQueryByte(intent, key, null)
    }

    fun getQueryByte(intent: Intent, key: String, defaultValue: Byte?): Byte? {
        return getQueryByte(intent.extras, key, defaultValue)
    }

    fun getQueryByte(bundle: Bundle?, key: String): Byte? {
        return getQueryByte(bundle, key, null)
    }

    fun getQueryByte(bundle: Bundle?, key: String, defaultValue: Byte?): Byte? {
        val values = getQueryBytes(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryBytes(intent: Intent, key: String): List<Byte> {
        return getQueryBytes(intent.extras, key)!!
    }

    fun getQueryBytes(bundle: Bundle?, key: String): List<Byte>? {
        return getQueries(bundle, key) { s -> s.toByte() }
    }

    fun getQueryChar(intent: Intent, key: String): Char? {
        return getQueryChar(intent, key, null)
    }

    fun getQueryChar(intent: Intent, key: String, defaultValue: Char?): Char? {
        return getQueryChar(intent.extras, key, defaultValue)
    }

    fun getQueryChar(bundle: Bundle?, key: String): Char? {
        return getQueryChar(bundle, key, null)
    }

    fun getQueryChar(bundle: Bundle?, key: String, defaultValue: Char?): Char? {
        val values = getQueryChars(bundle, key)
        return values?.firstOrNull() ?: defaultValue
    }

    fun getQueryChars(intent: Intent, key: String): List<Char> {
        return getQueryChars(intent.extras, key)!!
    }

    fun getQueryChars(bundle: Bundle?, key: String): List<Char>? {
        return getQueries(bundle, key) { s ->
            if (s.length == 1) {
                s[0]
            } else {
                throw IllegalArgumentException("$s is not a Character")
            }
        }
    }

    // ============================================================== 上面都是查询 query 的方法 ==============================================================
    fun getString(intent: Intent, key: String): String? {
        return getString(intent, key, null)
    }

    fun getString(intent: Intent, key: String, defaultValue: String?): String? {
        return getString(intent.extras, key, defaultValue)
    }

    fun getString(bundle: Bundle?, key: String): String? {
        return getString(bundle, key, null)
    }

    fun getString(bundle: Bundle?, key: String, defaultValue: String?): String? {
        if (bundle == null) {
            return defaultValue
        }
        var value = getQueryString(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getString(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getStringArrayList(intent: Intent, key: String): ArrayList<String>? {
        return getStringArrayList(intent, key, null)
    }

    fun getStringArrayList(
        intent: Intent,
        key: String,
        defaultValue: ArrayList<String>?,
    ): ArrayList<String>? {
        return getStringArrayList(intent.extras, key, defaultValue)
    }

    fun getStringArrayList(bundle: Bundle?, key: String): ArrayList<String>? {
        return getStringArrayList(bundle, key, null)
    }

    fun getStringArrayList(
        bundle: Bundle?,
        key: String,
        defaultValue: ArrayList<String>?,
    ): ArrayList<String>? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryStrings(bundle, key)
        var value = if (queryValues == null) null else ArrayList(queryValues)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getStringArrayList(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getInt(intent: Intent, key: String): Int? {
        return getInt(intent, key, null)
    }

    fun getInt(intent: Intent, key: String, defaultValue: Int?): Int? {
        return getInt(intent.extras, key, defaultValue)
    }

    fun getInt(bundle: Bundle?, key: String): Int? {
        return getInt(bundle, key, null)
    }

    fun getInt(bundle: Bundle?, key: String, defaultValue: Int?): Int? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Int?
        // 获取 query 中的
        value = getQueryInt(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getInt(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getIntegerArrayList(intent: Intent, key: String): ArrayList<Int>? {
        return getIntegerArrayList(intent, key, null)
    }

    fun getIntegerArrayList(
        intent: Intent,
        key: String,
        defaultValue: ArrayList<Int>?,
    ): ArrayList<Int>? {
        return getIntegerArrayList(intent.extras, key, defaultValue)
    }

    fun getIntegerArrayList(bundle: Bundle?, key: String): ArrayList<Int>? {
        return getIntegerArrayList(bundle, key, null)
    }

    fun getIntegerArrayList(
        bundle: Bundle?,
        key: String,
        defaultValue: ArrayList<Int>?,
    ): ArrayList<Int>? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryInts(bundle, key)
        var value = if (queryValues == null) null else ArrayList(queryValues)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getIntegerArrayList(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getLong(intent: Intent, key: String): Long? {
        return getLong(intent, key, null)
    }

    fun getLong(intent: Intent, key: String, defaultValue: Long?): Long? {
        return getLong(intent.extras, key, defaultValue)
    }

    fun getLong(bundle: Bundle?, key: String): Long? {
        return getLong(bundle, key, null)
    }

    fun getLong(bundle: Bundle?, key: String, defaultValue: Long?): Long? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Long?
        // 获取 query 中的
        value = getQueryLong(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getLong(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getDouble(intent: Intent, key: String): Double? {
        return getDouble(intent, key, null)
    }

    fun getDouble(intent: Intent, key: String, defaultValue: Double?): Double? {
        return getDouble(intent.extras, key, defaultValue)
    }

    fun getDouble(bundle: Bundle?, key: String): Double? {
        return getDouble(bundle, key, null)
    }

    fun getDouble(bundle: Bundle?, key: String, defaultValue: Double?): Double? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Double?
        // 获取 query 中的
        value = getQueryDouble(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getDouble(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getCharSequence(intent: Intent, key: String): CharSequence? {
        return getCharSequence(intent, key, null)
    }

    fun getCharSequence(intent: Intent, key: String, defaultValue: CharSequence?): CharSequence? {
        return getCharSequence(intent.extras, key, defaultValue)
    }

    fun getCharSequence(bundle: Bundle?, key: String): CharSequence? {
        return getCharSequence(bundle, key, null)
    }

    fun getCharSequence(bundle: Bundle?, key: String, defaultValue: CharSequence?): CharSequence? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getCharSequence(key)
        } else {
            //fixme 是否有更好的方法
            getQueryString(bundle, key) ?: defaultValue
        }
    }

    fun getByte(intent: Intent, key: String): Byte? {
        return getByte(intent, key, null)
    }

    fun getByte(intent: Intent, key: String, defaultValue: Byte?): Byte? {
        return getByte(intent.extras, key, defaultValue)
    }

    fun getByte(bundle: Bundle?, key: String): Byte? {
        return getByte(bundle, key, null)
    }

    fun getByte(bundle: Bundle?, key: String, defaultValue: Byte?): Byte? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Byte?
        // 获取 query 中的
        value = getQueryByte(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getByte(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getChar(intent: Intent, key: String): Char? {
        return getChar(intent, key, null)
    }

    fun getChar(intent: Intent, key: String, defaultValue: Char?): Char? {
        return getChar(intent.extras, key, defaultValue)
    }

    fun getChar(bundle: Bundle?, key: String): Char? {
        return getChar(bundle, key, null)
    }

    fun getChar(bundle: Bundle?, key: String, defaultValue: Char?): Char? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Char?
        // 获取 query 中的
        value = getQueryChar(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getChar(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getFloat(intent: Intent, key: String): Float? {
        return getFloat(intent, key, null)
    }

    fun getFloat(intent: Intent, key: String, defaultValue: Float?): Float? {
        return getFloat(intent.extras, key, defaultValue)
    }

    fun getFloat(bundle: Bundle?, key: String): Float? {
        return getFloat(bundle, key, null)
    }

    fun getFloat(bundle: Bundle?, key: String, defaultValue: Float?): Float? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Float?
        // 获取 query 中的
        value = getQueryFloat(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getFloat(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getShort(intent: Intent, key: String): Short? {
        return getShort(intent, key, null)
    }

    fun getShort(intent: Intent, key: String, defaultValue: Short?): Short? {
        return getShort(intent.extras, key, defaultValue)
    }

    fun getShort(bundle: Bundle?, key: String): Short? {
        return getShort(bundle, key, null)
    }

    fun getShort(bundle: Bundle?, key: String, defaultValue: Short?): Short? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Short?
        // 获取 query 中的
        value = getQueryShort(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getShort(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getBoolean(intent: Intent, key: String): Boolean? {
        return getBoolean(intent, key, null)
    }

    fun getBoolean(intent: Intent, key: String, defaultValue: Boolean?): Boolean? {
        return getBoolean(intent.extras, key, defaultValue)
    }

    fun getBoolean(bundle: Bundle?, key: String): Boolean? {
        return getBoolean(bundle, key, null)
    }

    fun getBoolean(bundle: Bundle?, key: String, defaultValue: Boolean?): Boolean? {
        if (bundle == null) {
            return defaultValue
        }
        var value: Boolean?
        // 获取 query 中的
        value = getQueryBoolean(bundle, key, null)
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getBoolean(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    // ======================================== Array 实现 ========================================
    fun getStringArray(intent: Intent, key: String): Array<String>? {
        return getStringArray(intent, key, null)
    }

    fun getStringArray(intent: Intent, key: String, defaultValue: Array<String>?): Array<String>? {
        return getStringArray(intent.extras, key, defaultValue)
    }

    fun getStringArray(bundle: Bundle?, key: String): Array<String>? {
        return getStringArray(bundle, key, null)
    }

    fun getStringArray(bundle: Bundle?, key: String, defaultValue: Array<String>?): Array<String>? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryStrings(bundle, key)
        var value = queryValues?.toTypedArray()
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getStringArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getCharSequenceArray(intent: Intent, key: String): Array<CharSequence>? {
        return getCharSequenceArray(intent, key, null)
    }

    fun getCharSequenceArray(
        intent: Intent,
        key: String,
        defaultValue: Array<CharSequence>?,
    ): Array<CharSequence>? {
        return getCharSequenceArray(intent.extras, key, defaultValue)
    }

    fun getCharSequenceArray(bundle: Bundle?, key: String): Array<CharSequence>? {
        return getCharSequenceArray(bundle, key, null)
    }

    fun getCharSequenceArray(
        bundle: Bundle?,
        key: String,
        defaultValue: Array<CharSequence>?,
    ): Array<CharSequence>? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getCharSequenceArray(key)
        } else {
            defaultValue
        }
    }

    fun getBooleanArray(intent: Intent, key: String): BooleanArray? {
        return getBooleanArray(intent, key, null)
    }

    fun getBooleanArray(intent: Intent, key: String, defaultValue: BooleanArray?): BooleanArray? {
        return getBooleanArray(intent.extras, key, defaultValue)
    }

    fun getBooleanArray(bundle: Bundle?, key: String): BooleanArray? {
        return getBooleanArray(bundle, key, null)
    }

    fun getBooleanArray(bundle: Bundle?, key: String, defaultValue: BooleanArray?): BooleanArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryBooleans(bundle, key)
        var value: BooleanArray? = null
        if (queryValues != null) {
            value = BooleanArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getBooleanArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getByteArray(intent: Intent, key: String): ByteArray? {
        return getByteArray(intent, key, null)
    }

    fun getByteArray(intent: Intent, key: String, defaultValue: ByteArray?): ByteArray? {
        return getByteArray(intent.extras, key, defaultValue)
    }

    fun getByteArray(bundle: Bundle?, key: String): ByteArray? {
        return getByteArray(bundle, key, null)
    }

    fun getByteArray(bundle: Bundle?, key: String, defaultValue: ByteArray?): ByteArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryBytes(bundle, key)
        var value: ByteArray? = null
        if (queryValues != null) {
            value = ByteArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getByteArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getCharArray(intent: Intent, key: String): CharArray? {
        return getCharArray(intent, key, null)
    }

    fun getCharArray(intent: Intent, key: String, defaultValue: CharArray?): CharArray? {
        return getCharArray(intent.extras, key, defaultValue)
    }

    fun getCharArray(bundle: Bundle?, key: String): CharArray? {
        return getCharArray(bundle, key, null)
    }

    fun getCharArray(bundle: Bundle?, key: String, defaultValue: CharArray?): CharArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryChars(bundle, key)
        var value: CharArray? = null
        if (queryValues != null) {
            value = CharArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getCharArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getShortArray(intent: Intent, key: String): ShortArray? {
        return getShortArray(intent, key, null)
    }

    fun getShortArray(intent: Intent, key: String, defaultValue: ShortArray?): ShortArray? {
        return getShortArray(intent.extras, key, defaultValue)
    }

    fun getShortArray(bundle: Bundle?, key: String): ShortArray? {
        return getShortArray(bundle, key, null)
    }

    fun getShortArray(bundle: Bundle?, key: String, defaultValue: ShortArray?): ShortArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryShorts(bundle, key)
        var value: ShortArray? = null
        if (queryValues != null) {
            value = ShortArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getShortArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getIntArray(intent: Intent, key: String): IntArray? {
        return getIntArray(intent, key, null)
    }

    fun getIntArray(intent: Intent, key: String, defaultValue: IntArray?): IntArray? {
        return getIntArray(intent.extras, key, defaultValue)
    }

    fun getIntArray(bundle: Bundle?, key: String): IntArray? {
        return getIntArray(bundle, key, null)
    }

    fun getIntArray(bundle: Bundle?, key: String, defaultValue: IntArray?): IntArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryInts(bundle, key)
        var value: IntArray? = null
        if (queryValues != null) {
            value = IntArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getIntArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getLongArray(intent: Intent, key: String): LongArray? {
        return getLongArray(intent, key, null)
    }

    fun getLongArray(intent: Intent, key: String, defaultValue: LongArray?): LongArray? {
        return getLongArray(intent.extras, key, defaultValue)
    }

    fun getLongArray(bundle: Bundle?, key: String): LongArray? {
        return getLongArray(bundle, key, null)
    }

    fun getLongArray(bundle: Bundle?, key: String, defaultValue: LongArray?): LongArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryLongs(bundle, key)
        var value: LongArray? = null
        if (queryValues != null) {
            value = LongArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getLongArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getFloatArray(intent: Intent, key: String): FloatArray? {
        return getFloatArray(intent, key, null)
    }

    fun getFloatArray(intent: Intent, key: String, defaultValue: FloatArray?): FloatArray? {
        return getFloatArray(intent.extras, key, defaultValue)
    }

    fun getFloatArray(bundle: Bundle?, key: String): FloatArray? {
        return getFloatArray(bundle, key, null)
    }

    fun getFloatArray(bundle: Bundle?, key: String, defaultValue: FloatArray?): FloatArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryFloats(bundle, key)
        var value: FloatArray? = null
        if (queryValues != null) {
            value = FloatArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getFloatArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getDoubleArray(intent: Intent, key: String): DoubleArray? {
        return getDoubleArray(intent, key, null)
    }

    fun getDoubleArray(intent: Intent, key: String, defaultValue: DoubleArray?): DoubleArray? {
        return getDoubleArray(intent.extras, key, defaultValue)
    }

    fun getDoubleArray(bundle: Bundle?, key: String): DoubleArray? {
        return getDoubleArray(bundle, key, null)
    }

    fun getDoubleArray(bundle: Bundle?, key: String, defaultValue: DoubleArray?): DoubleArray? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryDoubles(bundle, key)
        var value: DoubleArray? = null
        if (queryValues != null) {
            value = DoubleArray(queryValues.size)
            for (i in value.indices) {
                value[i] = queryValues[i]
            }
        }
        if (value == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getDoubleArray(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun getParcelableArray(intent: Intent, key: String): Array<Parcelable>? {
        return getParcelableArray(intent, key, null)
    }

    fun getParcelableArray(
        intent: Intent,
        key: String,
        defaultValue: Array<Parcelable>?,
    ): Array<Parcelable>? {
        return getParcelableArray(intent.extras, key, defaultValue)
    }

    fun getParcelableArray(bundle: Bundle?, key: String): Array<Parcelable>? {
        return getParcelableArray(bundle, key, null)
    }

    fun getParcelableArray(
        bundle: Bundle?,
        key: String,
        defaultValue: Array<Parcelable>?,
    ): Array<Parcelable>? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getParcelableArray(key)
        } else {
            defaultValue
        }
    }

    fun <T : Parcelable?> getParcelableArrayList(intent: Intent, key: String): ArrayList<T>? {
        return getParcelableArrayList(intent, key, null)
    }

    fun <T : Parcelable?> getParcelableArrayList(
        intent: Intent,
        key: String,
        defaultValue: ArrayList<T>?,
    ): ArrayList<T>? {
        return getParcelableArrayList(intent.extras, key, defaultValue)
    }

    fun <T : Parcelable?> getParcelableArrayList(
        bundle: Bundle?,
        key: String,
    ): ArrayList<T>? {
        return getParcelableArrayList(bundle, key, null)
    }

    fun <T : Parcelable?> getParcelableArrayList(
        bundle: Bundle?,
        key: String,
        defaultValue: ArrayList<T>?,
    ): ArrayList<T>? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getParcelableArrayList(key)
        } else {
            defaultValue
        }
    }

    fun <T : Parcelable?> getSparseParcelableArray(intent: Intent, key: String): SparseArray<T>? {
        return getSparseParcelableArray(intent, key, null)
    }


    fun <T : Parcelable?> getSparseParcelableArray(
        intent: Intent,
        key: String,
        defaultValue: SparseArray<T>?,
    ): SparseArray<T>? {
        return getSparseParcelableArray(intent.extras, key, defaultValue)
    }

    fun <T : Parcelable?> getSparseParcelableArray(
        bundle: Bundle?,
        key: String,
    ): SparseArray<T>? {
        return getSparseParcelableArray(bundle, key, null)
    }

    fun <T : Parcelable?> getSparseParcelableArray(
        bundle: Bundle?,
        key: String,
        defaultValue: SparseArray<T>?,
    ): SparseArray<T>? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getSparseParcelableArray(key)
        } else {
            defaultValue
        }
    }

    fun getCharSequenceArrayList(intent: Intent, key: String): ArrayList<CharSequence>? {
        return getCharSequenceArrayList(intent, key, null)
    }

    fun getCharSequenceArrayList(
        intent: Intent,
        key: String,
        defaultValue: ArrayList<CharSequence>?,
    ): ArrayList<CharSequence>? {
        return getCharSequenceArrayList(intent.extras, key, defaultValue)
    }

    fun getCharSequenceArrayList(bundle: Bundle?, key: String): ArrayList<CharSequence>? {
        return getCharSequenceArrayList(bundle, key, null)
    }

    fun getCharSequenceArrayList(
        bundle: Bundle?,
        key: String,
        defaultValue: ArrayList<CharSequence>?,
    ): ArrayList<CharSequence>? {
        if (bundle == null) {
            return defaultValue
        }
        val queryValues = getQueryStrings(bundle, key)
        var value = if (queryValues == null) null else ArrayList<CharSequence>(queryValues)
        if (queryValues == null) {
            value = if (bundle.containsKey(key)) {
                bundle.getCharSequenceArrayList(key)
            } else {
                defaultValue
            }
        }
        return value
    }

    fun <T : Parcelable?> getParcelable(
        intent: Intent,
        key: String,
    ): T? {
        return getParcelable(intent, key, null)
    }

    fun <T : Parcelable?> getParcelable(
        intent: Intent,
        key: String,
        defaultValue: T?,
    ): T? {
        return getParcelable(intent.extras, key, defaultValue)
    }

    fun <T : Parcelable?> getParcelable(
        bundle: Bundle?,
        key: String,
    ): T? {
        return getParcelable(bundle, key, null)
    }

    fun <T : Parcelable?> getParcelable(
        bundle: Bundle?,
        key: String,
        defaultValue: T?,
    ): T? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getParcelable(key)
        } else {
            defaultValue
        }
    }

    fun <T : Serializable?> getSerializable(
        intent: Intent,
        key: String,
    ): T? {
        return getSerializable(intent, key, null)
    }

    fun <T : Serializable?> getSerializable(
        intent: Intent,
        key: String,
        defaultValue: T?,
    ): T? {
        return getSerializable(intent.extras, key, defaultValue)
    }

    fun <T : Serializable?> getSerializable(
        bundle: Bundle?,
        key: String,
    ): T? {
        return getSerializable(bundle, key, null)
    }

    fun <T : Serializable?> getSerializable(
        bundle: Bundle?,
        key: String,
        defaultValue: T?,
    ): T? {
        if (bundle == null) {
            return defaultValue
        }
        return if (bundle.containsKey(key)) {
            bundle.getSerializable(key) as T?
        } else {
            defaultValue
        }
    }


}