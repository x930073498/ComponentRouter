package com.x930073498.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.x930073498.ProcessException
import com.x930073498.util.ComponentConstants
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.properties.Delegates

abstract class BaseProcessor : AbstractProcessor() {
    protected var filer: Filer by Delegates.notNull()
    protected var messager: Messager by Delegates.notNull()
    var types: Types by Delegates.notNull()

    var elements: Elements by Delegates.notNull()

    var mTypeElementString: TypeElement by Delegates.notNull()
    var mTypeElementInteger: TypeElement by Delegates.notNull()
    var mTypeElementList: TypeElement by Delegates.notNull()
    var mTypeElementArrayList: TypeElement by Delegates.notNull()
    var mTypeElementSparseArray: TypeElement by Delegates.notNull()
    var mTypeElementHashMap: TypeElement by Delegates.notNull()
    var mTypeElementHashSet: TypeElement by Delegates.notNull()

    var mClassNameString: ClassName by Delegates.notNull()
    var mClassNameList: ClassName by Delegates.notNull()
    var mClassNameArrayList: ClassName by Delegates.notNull()
    var mClassNameSparseArray: ClassName by Delegates.notNull()
    var mClassNameHashMap: ClassName by Delegates.notNull()
    var mClassNameHashSet: ClassName by Delegates.notNull()


    var mTypeNameString: TypeName by Delegates.notNull()

    var serializableTypeMirror: TypeMirror by Delegates.notNull()
    var parcelableTypeMirror: TypeMirror by Delegates.notNull()

    var parameterSupportTypeMirror: TypeMirror by Delegates.notNull()
    var charSequenceTypeName: TypeName by Delegates.notNull()
    var charSequenceTypeElement: TypeElement by Delegates.notNull()
    var charSequenceTypeMirror: TypeMirror by Delegates.notNull()
    var arrayListTypeElement: TypeElement by Delegates.notNull()
    var arrayListClassName: ClassName by Delegates.notNull()
    var host: String by Delegates.notNull()
    var generatePackageName="com.x930073498.router"

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        host = processingEnv.options["authority"] ?: ""
        generatePackageName=processingEnv.options["packageName"]?:generatePackageName
        if (host.isEmpty()) {
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

        mClassNameString = mTypeElementString.asClassName()
        mClassNameList = mTypeElementList.asClassName()
        mClassNameArrayList = mTypeElementArrayList.asClassName()
        mClassNameSparseArray = mTypeElementSparseArray.asClassName()
        mClassNameHashMap = mTypeElementHashMap.asClassName()
        mClassNameHashSet = mTypeElementHashSet.asClassName()

        mTypeNameString = mTypeElementString.asClassName()

        val serializableTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.JAVA_SERIALIZABLE)
        serializableTypeMirror = serializableTypeElement.asType()
        val parcelableTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.ANDROID_PARCELABLE)
        parcelableTypeMirror = parcelableTypeElement.asType()
        val parameterSupportTypeElement: TypeElement =
            elements.getTypeElement(ComponentConstants.PARAMETERSUPPORT_CLASS_NAME)
        parameterSupportTypeMirror = parameterSupportTypeElement.asType()
        charSequenceTypeElement = elements.getTypeElement(ComponentConstants.JAVA_CHARSEQUENCE)
        charSequenceTypeMirror = charSequenceTypeElement.asType()
        charSequenceTypeName = charSequenceTypeMirror.asTypeName()
        arrayListTypeElement = elements.getTypeElement(ComponentConstants.JAVA_ARRAYLIST)
        arrayListClassName = arrayListTypeElement.asClassName()
        messager.printMessage(Diagnostic.Kind.WARNING, "host=$host")
    }

}