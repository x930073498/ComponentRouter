package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.x930073498.annotations.FragmentAnnotation
import com.x930073498.annotations.ModuleRegisterAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.util.*
import java.lang.StringBuilder
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import kotlin.math.absoluteValue

@Suppress("SameParameterValue")
@AutoService(Processor::class)
@SupportedAnnotationTypes("com.x930073498.annotations.FragmentAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class FragmentAnnotationProcessor : BaseProcessor() {

    @KotlinPoetMetadataPreview
    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment,
    ): Boolean {
        val routeElements =
            roundEnvironment.getElementsAnnotatedWith(FragmentAnnotation::class.java)
        if (routeElements.isEmpty()) {
            return false
        }
        messager.printMessage(Diagnostic.Kind.OTHER, "开始生成")


        val centerClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ActionCenter")
        val registerFunction = FunSpec.builder("register")
            .addModifiers(KModifier.OVERRIDE)
        val registerString = StringBuilder()
        routeElements.forEach {
            it as TypeElement
            messager.printMessage(Diagnostic.Kind.OTHER, "开始生成$it\n")
            val packName = elements.getPackageOf(it).qualifiedName.toString()
            val fragmentAnnotation = it.getAnnotation(FragmentAnnotation::class.java)
            val name = "_$$" + it.simpleName.toString() + "FragmentActionDelegateGenerated"
            registerString.append(packName).append(name)
            buildType(packName, name, it, fragmentAnnotation, registerFunction, centerClassName)
        }
        val registerType =
            TypeSpec.classBuilder("_$$${host.capitalize(Locale.getDefault())}${
                registerString.toString().hashCode().absoluteValue
            }FragmentRegisterGenerated")
                .addSuperinterface(ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
                    "Register"))
                .addAnnotation(ModuleRegisterAnnotation::class)
                .addFunction(registerFunction.build())
        FileSpec.get(generatePackageName, registerType.build()).writeTo(filer)
        return false
    }


    private fun buildType(
        pkgName: String,
        name: String,
        element: TypeElement,
        fragmentAnnotation: FragmentAnnotation,
        registerFunction: FunSpec.Builder,
        centerClassName: ClassName,
    ) {

        val typeSpec = TypeSpec.classBuilder(name)
        val fragmentDelegateType =
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, "FragmentActionDelegate")
                .parameterizedBy(element.asClassName())
        typeSpec.addSuperinterface(fragmentDelegateType)
        typeSpec.addAnnotation(ModuleRegisterAnnotation::class.java)
        buildInjectFunction(typeSpec, element, fragmentAnnotation)
        buildKeyProperty(typeSpec, element, fragmentAnnotation)
        buildTargetFunction(typeSpec, element, fragmentAnnotation)
        buildFactoryFunction(typeSpec, element, fragmentAnnotation)
        val type = typeSpec.build()
        FileSpec.get(pkgName, type).writeTo(filer)
        registerFunction.addStatement("%T.register(%T())",
            centerClassName,
            ClassName(pkgName, name))
    }

    private fun buildInjectFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: FragmentAnnotation,
    ) {
        val inject = FunSpec.builder("inject")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec("bundle",
                ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)))
            .addParameter(ParameterSpec.builder("fragment", element.asClassName()).build())
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
                "bundle", "fragment")
        }


    }


    private fun buildKeyProperty(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: FragmentAnnotation,
    ) {
        buildKeyProperty(typeSpec,
            fragmentAnnotation.authority,
            fragmentAnnotation.path)
    }

    private fun buildTargetFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: FragmentAnnotation,
    ) {
        val targetClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "FragmentTarget")
        buildTargetFunction(targetClassName, element, typeSpec)
    }


    private fun buildFactoryFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: FragmentAnnotation,
    ) {
        val factoryClassName =
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
                "FragmentActionDelegate.Factory")
                .parameterizedBy(element.asClassName())

        val contextHolderClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ContextHolder")
        val clazzClassName = Class::class.asClassName().parameterizedBy(element.asClassName())
        val bundleClassName = ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)
        val factoryObject = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(factoryClassName)
            .addFunction(FunSpec.builder("create")
                .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                .addParameter("contextHolder", contextHolderClassName)
                .addParameter("clazz", clazzClassName)
                .addParameter("bundle", bundleClassName)
//                .returns(element.asClassName().copy(nullable = true))
                .addStatement("return %T().apply{arguments=bundle}", element)
                .build())
            .build()

        val factory = FunSpec.builder("factory")
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .addStatement("return %L", factoryObject)
        typeSpec.addFunction(factory.build())

    }


}