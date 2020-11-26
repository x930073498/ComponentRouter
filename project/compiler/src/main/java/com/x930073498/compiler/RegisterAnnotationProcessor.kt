package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.x930073498.annotations.*
import com.x930073498.util.*
import java.lang.StringBuilder
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

@Suppress("SameParameterValue")
@AutoService(Processor::class)
@SupportedAnnotationTypes(
    "com.x930073498.annotations.ActivityRegister",
    "com.x930073498.annotations.MethodRegister",
    "com.x930073498.annotations.FragmentRegister",
    "com.x930073498.annotations.ServiceRegister",
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RegisterAnnotationProcessor : BaseProcessor() {
    val packageName = "routes"

    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment,
    ): Boolean {
        var list = arrayListOf<Element>()
        set.forEach {
            list.addAll(roundEnvironment.getElementsAnnotatedWith(it))
        }
        if (list.isEmpty()) return false
        list.forEach {
            it as TypeElement
        }
        return false
    }


}