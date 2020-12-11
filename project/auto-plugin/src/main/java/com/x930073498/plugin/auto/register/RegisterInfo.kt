package com.x930073498.plugin.auto.register

import java.io.File
import java.util.regex.Pattern

class RegisterInfo {
    companion object {
        val DEFAULT_EXCLUDE = arrayListOf(".*/R(\\\$[^/]*)?", "'.*/BuildConfig\$'")
    }


    private var include = arrayListOf<String>()
    private var exclude = arrayListOf<String>()

    var includePatterns = arrayListOf<Pattern>()
    var excludePatterns = arrayListOf<Pattern>()
    var fileContainsInitClass: File? = null
    var classList = arrayListOf<String>()

    fun reset() {
        fileContainsInitClass = null
        classList.clear()
    }


    override fun toString(): String {
        return buildString {
            append("{")
            append("\n\t")
                .append("scanInterface")
                .append("\t\t\t=\t")
                .append(INTERFACE_NAME_SCAN)

            append(" ]")
            append("\n\t")
                .append("codeInsertToClassName")
                .append("\t=\t")
                .append(CLASS_NAME_CODE_INSERT_TO)
            append("\n\t")
                .append("codeInsertToMethodName")
                .append("\t=\t")
                .append(METHOD_NAME_CODE_INSERT_TO)
            append("\n\t")
                .append("registerMethodName")
                .append("\t\t=\tpublic static void ")
                .append(CLASS_NAME_CODE_INSERT_TO)
                .append(".")
                .append(METHOD_NAME_REGISTER)
            append("\n\t")
                .append("include")
                .append(" = [")
            include.forEach {
                append("\n\t\t'").append(it).append("\'")
            }
            append("\n\t]")
            append("\n\t").append("exclude").append(" = [")
            exclude.forEach {
                append("\n\t\t\'").append(it).append("\'")
            }
            append("\n\t]\n}")
        }
    }

    fun init() {
        if (include.isEmpty()) {
            include.add(".*")
        }
        DEFAULT_EXCLUDE.forEach {
            if (!exclude.contains(it))
                exclude.add(it)
        }
        initPattern(include, includePatterns)
        initPattern(exclude, excludePatterns)
    }


    private fun initPattern(list: List<String>, patterns: MutableList<Pattern>) {
        list.forEach {
            patterns.add(Pattern.compile(it))
        }
    }
}