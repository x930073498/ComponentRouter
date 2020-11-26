package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.x930073498.annotations.*
import com.x930073498.bean.toInfo
import com.x930073498.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

@Suppress("SameParameterValue")
@AutoService(Processor::class)
@SupportedAnnotationTypes("com.x930073498.annotations.ServiceAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ServiceAnnotationProcessor : BaseProcessor() {

    @KotlinPoetMetadataPreview
    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment,
    ): Boolean {
        val routeElements =
            roundEnvironment.getElementsAnnotatedWith(ServiceAnnotation::class.java)
        if (routeElements.isEmpty()) {
            return false
        }
        messager.printMessage(Diagnostic.Kind.OTHER, "开始生成")
        routeElements.forEach {
            it as TypeElement
            messager.printMessage(Diagnostic.Kind.OTHER, "开始生成$it\n")
            val packName = elements.getPackageOf(it).qualifiedName.toString()
            val serviceAnnotation = it.getAnnotation(ServiceAnnotation::class.java)
            val name = "_$$" + it.simpleName.toString() + "ServiceActionDelegateGenerated"
            buildType(packName, name, it, serviceAnnotation)
        }

        return false
    }

    private fun buildType(
        pkgName: String,
        name: String,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {

        val typeSpec = TypeSpec.classBuilder(name)
        val serviceDelegateType =
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, "ServiceActionDelegate")
                .parameterizedBy(element.asClassName())
        typeSpec.addSuperinterface(serviceDelegateType)
        buildInjectFunction(typeSpec, element, serviceAnnotation)
        buildKeyProperty(typeSpec, element, serviceAnnotation)
        buildTargetFunction(typeSpec, element, serviceAnnotation)
        buildAutoInvokeFunction(typeSpec, element, serviceAnnotation)
        buildFactoryFunction(typeSpec, element, serviceAnnotation)
        buildToStringFunction(element, typeSpec)
        FileSpec.get(pkgName, typeSpec.build()).writeTo(filer)
    }

    private fun buildInjectFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {

        val inject = FunSpec.builder("inject")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec("bundle",
                ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)))
            .addParameter(ParameterSpec.builder("provider", element.asClassName()).build())
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
                "bundle", "provider")
        }


    }


    private fun buildKeyProperty(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {
        val info =
            serviceAnnotation.toInfo() ?: return messager.printMessage(Diagnostic.Kind.ERROR,
                "获取信息出错")
        buildKeyProperty(typeSpec, info)
    }

    private fun buildTargetFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {
        val fragmentTargetClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ServiceTarget")
        buildTargetFunction(fragmentTargetClassName, element, serviceAnnotation, typeSpec)
    }


    private fun buildAutoInvokeFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {
        val autoInvoke = FunSpec.builder("autoInvoke")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", serviceAnnotation.autoInvoke)
        typeSpec.addFunction(autoInvoke.build())
    }

    private fun buildFactoryFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {
        val factoryClassName =
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
                "ServiceActionDelegate.Factory")
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
                .addStatement("return %T()", element)
                .build())
            .build()

        val factory = FunSpec.builder("factory")
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .addStatement("return %L", factoryObject)
        typeSpec.addFunction(factory.build())

    }


}