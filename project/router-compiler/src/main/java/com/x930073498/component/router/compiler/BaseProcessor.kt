package com.x930073498.component.router.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.x930073498.component.router.data.RouterDoc
import com.x930073498.component.router.util.ComponentConstants
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
    var mTypeElementSparseArray: TypeElement by Delegates.notNull()

    var mTypeNameString: TypeName by Delegates.notNull()
    var mTypeNameSparseArray: TypeName by Delegates.notNull()


    var serializableTypeMirror: TypeMirror by Delegates.notNull()
    var parcelableTypeMirror: TypeMirror by Delegates.notNull()
    var contextTypeMirror: TypeMirror by Delegates.notNull()
    var fragmentTypeMirror: TypeMirror by Delegates.notNull()
    var activityTypeMirror: TypeMirror by Delegates.notNull()

    var parameterSupportTypeMirror: TypeMirror by Delegates.notNull()
    var charSequenceTypeName: TypeName by Delegates.notNull()
    var charSequenceTypeElement: TypeElement by Delegates.notNull()
    var charSequenceTypeMirror: TypeMirror by Delegates.notNull()
    var isDocEnable = false
    var projectName = ""
    val docList by lazy {
        arrayListOf<RouterDoc>()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elements = processingEnv.elementUtils
        types = processingEnv.typeUtils
        messager = processingEnv.messager
        filer = processingEnv.filer

        isDocEnable =
            runCatching { processingEnv.options["router_document_enable"].toBoolean() }.getOrElse { false }
        projectName =
            runCatching { processingEnv.options["router_document_project_name"].toString() }.getOrElse { "" }
        mTypeElementString = elements.getTypeElement(ComponentConstants.JAVA_STRING)
        mTypeElementInteger = elements.getTypeElement(ComponentConstants.JAVA_INTEGER)
        mTypeElementSparseArray = elements.getTypeElement(ComponentConstants.ANDROID_SPARSEARRAY)
        mTypeNameString = mTypeElementString.javaToKotlinType()
        mTypeNameSparseArray = mTypeElementSparseArray.javaToKotlinType()
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
        fragmentTypeMirror = elements.getTypeElement(ComponentConstants.ANDROID_FRAGMENT).asType()
        activityTypeMirror = elements.getTypeElement(ComponentConstants.ANDROID_ACTIVITY).asType()
        charSequenceTypeName = charSequenceTypeMirror.asTypeName().javaToKotlinType()
        arrayListTypeElement = elements.getTypeElement(ComponentConstants.JAVA_ARRAYLIST)
        arrayListClassName = arrayListTypeElement.javaToKotlinType()
    }

    var arrayListTypeElement: TypeElement by Delegates.notNull()

    var arrayListClassName: TypeName by Delegates.notNull()

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