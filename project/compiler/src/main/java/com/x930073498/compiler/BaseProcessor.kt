package com.x930073498.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.x930073498.ProcessException
import com.x930073498.util.ComponentConstants
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.name.FqName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.properties.Delegates


abstract class BaseProcessor : AbstractProcessor() {
     var filer: Filer by Delegates.notNull()
    var messager: Messager by Delegates.notNull()
    var types: Types by Delegates.notNull()

    var elements: Elements by Delegates.notNull()

    var mTypeElementString: TypeElement by Delegates.notNull()
    var mTypeElementInteger: TypeElement by Delegates.notNull()
    var mTypeElementList: TypeElement by Delegates.notNull()
    var mTypeElementArrayList: TypeElement by Delegates.notNull()
    var mTypeElementSparseArray: TypeElement by Delegates.notNull()
    var mTypeElementHashMap: TypeElement by Delegates.notNull()
    var mTypeElementHashSet: TypeElement by Delegates.notNull()

    var mTypeNameString: TypeName by Delegates.notNull()
    var mTypeNameList: TypeName by Delegates.notNull()
    var mTypeNameArrayList: TypeName by Delegates.notNull()
    var mTypeNameSparseArray: TypeName by Delegates.notNull()
    var mTypeNameHashMap: TypeName by Delegates.notNull()
    var mTypeNameHashSet: TypeName by Delegates.notNull()


    var serializableTypeMirror: TypeMirror by Delegates.notNull()
    var parcelableTypeMirror: TypeMirror by Delegates.notNull()
    var contextTypeMirror: TypeMirror by Delegates.notNull()

    var parameterSupportTypeMirror: TypeMirror by Delegates.notNull()
    var charSequenceTypeName: TypeName by Delegates.notNull()
    var charSequenceTypeElement: TypeElement by Delegates.notNull()
    var charSequenceTypeMirror: TypeMirror by Delegates.notNull()
    var arrayListTypeElement: TypeElement by Delegates.notNull()
    var arrayListClassName: TypeName by Delegates.notNull()
    var moduleName: String by Delegates.notNull()
    var generatePackageName = "com.x930073498.router"

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        moduleName = processingEnv.options["AROUTER_MODULE_NAME"] ?: ""
//        generatePackageName=processingEnv.options["packageName"]?:generatePackageName
        if (moduleName.isEmpty()) {
            throw ProcessException("模块尚未配置authority属性")
        }
        elements = processingEnv.elementUtils
        types = processingEnv.typeUtils
        messager = processingEnv.messager
        filer = processingEnv.filer
        mTypeElementString = elements.getTypeElement(ComponentConstants.JAVA_STRING)
        mTypeElementInteger = elements.getTypeElement(ComponentConstants.JAVA_INTEGER)
        mTypeElementList = elements.getTypeElement(ComponentConstants.JAVA_LIST)
        mTypeElementArrayList = elements.getTypeElement(ComponentConstants.JAVA_ARRAYLIST)
        mTypeElementSparseArray = elements.getTypeElement(ComponentConstants.ANDROID_SPARSEARRAY)
        mTypeElementHashMap = elements.getTypeElement(ComponentConstants.JAVA_HASHMAP)
        mTypeElementHashSet = elements.getTypeElement(ComponentConstants.JAVA_HASHSET)

        mTypeNameString = mTypeElementString.javaToKotlinType()
        mTypeNameList = mTypeElementList.javaToKotlinType()
        mTypeNameArrayList = mTypeElementArrayList.javaToKotlinType()
        mTypeNameSparseArray = mTypeElementSparseArray.javaToKotlinType()
        mTypeNameHashMap = mTypeElementHashMap.javaToKotlinType()
        mTypeNameHashSet = mTypeElementHashSet.javaToKotlinType()


        val serializableTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.JAVA_SERIALIZABLE)
        serializableTypeMirror = serializableTypeElement.asType()
        val parcelableTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.ANDROID_PARCELABLE)
        parcelableTypeMirror = parcelableTypeElement.asType()
        val contextTypeElement = elements.getTypeElement(ComponentConstants.ANDROID_CONTEXT)
        contextTypeMirror = contextTypeElement.asType()
        val parameterSupportTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.PARAMETER_SUPPORT_CLASS_NAME)
        parameterSupportTypeMirror = parameterSupportTypeElement.asType()
        charSequenceTypeElement = elements.getTypeElement(ComponentConstants.JAVA_CHARSEQUENCE)
        charSequenceTypeMirror = charSequenceTypeElement.asType()
        charSequenceTypeName = charSequenceTypeMirror.asTypeName().javaToKotlinType()
        arrayListTypeElement = elements.getTypeElement(ComponentConstants.JAVA_ARRAYLIST)
        arrayListClassName = arrayListTypeElement.javaToKotlinType()
        messager.printMessage(Diagnostic.Kind.WARNING, "host=$moduleName\n")
    }

    protected fun TypeMirror.javaToKotlinType(): TypeName {
        return asTypeName().javaToKotlinType()
    }

    fun Element.javaToKotlinType(): TypeName =
        asType().asTypeName().javaToKotlinType()

    fun TypeName.javaToKotlinType(): TypeName {
        return if (this is ParameterizedTypeName) {
            (rawType.javaToKotlinType() as ClassName)
                .parameterizedBy(*typeArguments.map { it.javaToKotlinType() }
                    .toTypedArray())
        } else {
            val className =
                JavaToKotlinClassMap.mapJavaToKotlin(FqName(toString()))
                    ?.asSingleFqName()?.asString()

            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}