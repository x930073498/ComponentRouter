package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.x930073498.annotations.*
import com.x930073498.bean.toInfo
import com.x930073498.util.*
import java.lang.StringBuilder
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

@Suppress("SameParameterValue")
@AutoService(Processor::class)
@SupportedAnnotationTypes("com.x930073498.annotations.ActivityAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ActivityAnnotationProcessor : BaseProcessor() {
    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment,
    ): Boolean {
        val routeElements =
            roundEnvironment.getElementsAnnotatedWith(ActivityAnnotation::class.java)
        if (routeElements.isEmpty()) {
            return false
        }
        messager.printMessage(Diagnostic.Kind.OTHER, "开始生成")
        val centerClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ActionCenter")
        val registerFunction = FunSpec.builder("register")
            .addModifiers(KModifier.PRIVATE)
        val registerString = StringBuilder()
        routeElements.forEach {
            it as TypeElement
            messager.printMessage(Diagnostic.Kind.OTHER, "开始生成$it\n")
            val packName = elements.getPackageOf(it).qualifiedName.toString()
            val activityAnnotation = it.getAnnotation(ActivityAnnotation::class.java)
            val name = "_$$" + it.simpleName.toString() + "ActivityActionDelegateGenerated"
            registerString.append(packName).append(name)
            buildType(packName, name, it, activityAnnotation, registerFunction, centerClassName)
        }

        val registerClassName = "_$$${moduleName.capitalize(Locale.getDefault())}_${
            registerString.toString().hashCode().absoluteValue
        }_ActivityRegisterGenerated"
        createRegisterClass(registerClassName,
            centerClassName,
            registerFunction,
            ActivityRegister::class)
        return false
    }

    private fun createRegisterClass(
        registerClassName: String,
        centerClassName: ClassName,
        registerFunction: FunSpec.Builder,
        annotationClazz: KClass<*>,
    ) {
        val registerClassType = ClassName("$generatePackageName.register", registerClassName)
        val registerType =
            TypeSpec.classBuilder(registerClassName)
                .addProperty(PropertySpec.builder("keys",
                    ArrayList::class.asTypeName()
                        .parameterizedBy(ClassName("com.x930073498.router.action", "Key")),
                    KModifier.PRIVATE)
                    .initializer("arrayListOf()")
                    .build())
                .addType(TypeSpec.companionObjectBuilder()
                    .addProperty(PropertySpec.builder("register", registerClassType)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("%T()", registerClassType)
                        .build())
                    .addFunction(FunSpec.builder("register").addStatement("register.register()")
                        .build())
                    .addFunction(FunSpec.builder("unregister").addStatement("register.unregister()")
                        .build())
                    .build())
                .addAnnotation(annotationClazz)
                .addFunction(FunSpec.builder("unregister")
                    .addStatement("keys.forEach{%T.unregister(it)}", centerClassName)
                    .addStatement("keys.clear()")
                    .build())
                .addFunction(registerFunction.build())
        FileSpec.get("$generatePackageName.register", registerType.build()).writeTo(filer)
    }

    private fun buildType(
        pkgName: String,
        name: String,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
        registerFunction: FunSpec.Builder,
        centerClassName: ClassName,
    ) {

        val typeSpec = TypeSpec.classBuilder(name)
        val fragmentDelegateType =
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, "ActivityActionDelegate")
                .parameterizedBy(element.asClassName())
        typeSpec.addSuperinterface(fragmentDelegateType)
//        typeSpec.superclass(ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,"AutoAction").parameterizedBy(element.asClassName()))
//        typeSpec.addSuperinterface(ClassName.bestGuess(ComponentConstants.AUTO_INTERFACE_NAME))
        buildInjectFunction(typeSpec, element, activityAnnotation)
        buildKeyProperty(typeSpec, element, activityAnnotation)
        buildTargetFunction(typeSpec, element, activityAnnotation)
//        buildFactoryFunction(typeSpec, element, fragmentAnnotation)
        buildToStringFunction(element, typeSpec)
        val type = typeSpec.build()
        FileSpec.get(pkgName, type).writeTo(filer)
        registerFunction.addStatement("%T.register(%T()).apply{keys.add(this)}",
            centerClassName,
            ClassName(pkgName, name))
    }

    private fun buildInjectFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
    ) {

        val inject = FunSpec.builder("inject")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec("intent",
                ClassName.bestGuess(ComponentConstants.ANDROID_INTENT)))
            .addParameter(ParameterSpec.builder("activity", element.asClassName()).build())
        buildInjectAttrFunction(inject, element)
        typeSpec.addFunction(inject.build())
    }

    private fun buildInjectAttrFunction(inject: FunSpec.Builder, element: TypeElement) {

        element.enclosedElements.mapNotNull {
            val annotation = it.getAnnotation(ValueAutowiredAnnotation::class.java)
            if (annotation != null) annotation to it
            else null
        }.forEach {
            messager.printMessage(Diagnostic.Kind.OTHER, "获取到注入元素$it \n")
            generateParameterCodeForInject(it.second as VariableElement,
                inject,
                it.second.simpleName.toString(),
                "intent", "activity")
        }


    }


    private fun buildKeyProperty(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
    ) {
        val info =
            activityAnnotation.toInfo() ?: return messager.printMessage(Diagnostic.Kind.ERROR,
                "获取信息出错")
        buildKeyProperty(typeSpec, info)
    }

    private fun buildTargetFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
    ) {
        val targetClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ActivityTarget")
        buildTargetFunction(targetClassName, element, typeSpec)
    }


}