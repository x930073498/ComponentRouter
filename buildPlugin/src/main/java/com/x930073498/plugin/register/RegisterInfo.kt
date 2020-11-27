package com.x930073498.plugin.register

import java.io.File
import java.util.regex.Pattern

 class RegisterInfo {
    companion object {
        val DEFAULT_EXCLUDE = arrayListOf(".*/R(\\\$[^/]*)?", "'.*/BuildConfig\$'")
    }

    var interfaceName = ""
    var superClassNames = mutableListOf<String>()
    var initClassName = ""
    var initMethodName = ""
    var registerClassName = ""
    var registerMethodName = ""
    var include = arrayListOf<String>()
    var exclude = arrayListOf<String>()

    var includePatterns = arrayListOf<Pattern>()
    var excludePatterns = arrayListOf<Pattern>()
    var fileContainsInitClass: File? = null
    var classList = arrayListOf<String>()

    fun reset() {
        fileContainsInitClass = null
        classList.clear()
    }

    fun validate(): Boolean {
        return interfaceName.isNotEmpty() && registerClassName.isNotEmpty() && registerMethodName.isNotEmpty()
    }

    override fun toString(): String {
        return buildString {
            append("{")
            append("\n\t")
                .append("scanInterface")
                .append("\t\t\t=\t")
                .append(interfaceName)

            append("\n\t")
                .append("scanSuperClasses")
                .append("\t\t=\t[")
                .run {
                    superClassNames.forEachIndexed { index, s ->
                        if (index > 0) append(",")
                        append("\'").append(s).append("\'")
                    }
                }
            append(" ]")
            append("\n\t")
                .append("codeInsertToClassName")
                .append("\t=\t")
                .append(initClassName)
            append("\n\t")
                .append("codeInsertToMethodName")
                .append("\t=\t")
                .append(initMethodName)
            append("\n\t")
                .append("registerMethodName")
                .append("\t\t=\tpublic static void ")
                .append(registerClassName)
                .append(".")
                .append(registerMethodName)
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
        if (registerClassName.isEmpty()) {
            registerClassName = initClassName
        }
        if (interfaceName.isNotEmpty()) {
            interfaceName = convertDotToSlash(interfaceName)
        }
        superClassNames = superClassNames.map {
            convertDotToSlash(it)
        }.toMutableList()
        initClassName = convertDotToSlash(initClassName)
        if (initMethodName.isEmpty()) {
            initMethodName = "<clinit>"
        }
        registerClassName = convertDotToSlash(registerClassName)
        DEFAULT_EXCLUDE.forEach {
            if (!exclude.contains(it))
                exclude.add(it)
        }
        initPattern(include, includePatterns)
        initPattern(exclude, excludePatterns)
    }

    fun convertDotToSlash(str: String): String {
        return str.replace(".", "/").intern()
    }

    fun initPattern(list: List<String>, patterns: MutableList<Pattern>) {
        list.forEach {
            patterns.add(Pattern.compile(it))
        }
    }
}