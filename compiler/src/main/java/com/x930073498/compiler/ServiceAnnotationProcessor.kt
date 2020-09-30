package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.x930073498.annotations.*
import com.x930073498.util.ComponentConstants
import com.x930073498.util.buildKeyProperty
import com.x930073498.util.buildTargetFunction
import com.x930073498.util.generateParameterCodeForInject
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
            val fragmentAnnotation = it.getAnnotation(ServiceAnnotation::class.java)
            val name = "_$$" + it.simpleName.toString() + "ServiceActionDelegateGenerated"
            buildType(packName, name, it, fragmentAnnotation)
        }

        return false
    }

    private fun buildType(
        pkgName: String,
        name: String,
        element: TypeElement,
        fragmentAnnotation: ServiceAnnotation,
    ) {

        val typeSpec = TypeSpec.classBuilder(name)
        val fragmentDelegateType = ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, "ServiceActionDelegate")
            .parameterizedBy(element.asClassName())
        typeSpec.addSuperinterface(fragmentDelegateType)
        typeSpec.addAnnotation(ModuleRegisterAnnotation::class.java)
        buildInjectFunction(typeSpec, element, fragmentAnnotation)
        buildKeyProperty(typeSpec, element, fragmentAnnotation)
        buildTargetFunction(typeSpec, element, fragmentAnnotation)
        buildAutoInvokeFunction(typeSpec, element, fragmentAnnotation)
        buildFactoryFunction(typeSpec, element, fragmentAnnotation)
        FileSpec.get(pkgName, typeSpec.build()).writeTo(filer)
    }

    private fun buildInjectFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: ServiceAnnotation,
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
        buildKeyProperty(typeSpec,
            serviceAnnotation.authority,
            serviceAnnotation.path)
    }

    private fun buildTargetFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        serviceAnnotation: ServiceAnnotation,
    ) {
        val fragmentTargetClassName = ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ServiceTarget")
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
            ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,"ServiceActionDelegate.Factory")
                .parameterizedBy(element.asClassName())

        val contextHolderClassName =
            ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME,"ContextHolder")
        val clazzClassName = Class::class.asClassName().parameterizedBy(element.asClassName())
        val bundleClassName=ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)
        val factoryObject = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(factoryClassName)
            .addFunction(FunSpec.builder("create")
                .addModifiers(KModifier.SUSPEND,KModifier.OVERRIDE)
                .addParameter("contextHolder",contextHolderClassName)
                .addParameter("clazz",clazzClassName)
                .addParameter("bundle",bundleClassName)
                .addStatement("return %T()",element)
                .build())
            .build()

        val factory = FunSpec.builder("factory")
            .addModifiers(KModifier.SUSPEND,KModifier.OVERRIDE)
            .addStatement("return %L",factoryObject)
        typeSpec.addFunction(factory.build())

    }


}