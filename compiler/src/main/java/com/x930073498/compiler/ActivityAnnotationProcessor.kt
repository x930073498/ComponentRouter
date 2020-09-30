package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.x930073498.annotations.ActivityAnnotation
import com.x930073498.annotations.ModuleRegisterAnnotation
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

@Suppress( "SameParameterValue")
@AutoService(Processor::class)
@SupportedAnnotationTypes("com.x930073498.annotations.ActivityAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ActivityAnnotationProcessor : BaseProcessor() {



    @KotlinPoetMetadataPreview
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
        routeElements.forEach {
            it as TypeElement
            messager.printMessage(Diagnostic.Kind.OTHER, "开始生成$it\n")
            val packName = elements.getPackageOf(it).qualifiedName.toString()
            val fragmentAnnotation = it.getAnnotation(ActivityAnnotation::class.java)
            val name = "_$$" + it.simpleName.toString() + "ActivityActionDelegateGenerated"
            buildType(packName, name, it, fragmentAnnotation)
        }

        return false
    }

    private fun buildType(
        pkgName: String,
        name: String,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
    ) {

        val typeSpec = TypeSpec.classBuilder(name)
        val fragmentDelegateType = ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, "ActivityActionDelegate")
            .parameterizedBy(element.asClassName())
        typeSpec.addSuperinterface(fragmentDelegateType)
        typeSpec.addAnnotation(ModuleRegisterAnnotation::class.java)
        buildInjectFunction(typeSpec, element, activityAnnotation)
        buildKeyProperty(typeSpec, element, activityAnnotation)
        buildTargetFunction(typeSpec, element, activityAnnotation)
//        buildFactoryFunction(typeSpec, element, fragmentAnnotation)
        FileSpec.get(pkgName, typeSpec.build()).writeTo(filer)
    }

    private fun buildInjectFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        fragmentAnnotation: ActivityAnnotation,
    ) {

        val inject = FunSpec.builder("inject")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec("intent", ClassName.bestGuess(ComponentConstants.ANDROID_INTENT)))
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
     buildKeyProperty(typeSpec,activityAnnotation.authority,activityAnnotation.path)
    }

    private fun buildTargetFunction(
        typeSpec: TypeSpec.Builder,
        element: TypeElement,
        activityAnnotation: ActivityAnnotation,
    ) {
        val targetClassName = ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, "ActivityTarget")
        buildTargetFunction(targetClassName, element, typeSpec)
    }

//    private fun buildFactoryFunction(
//        typeSpec: TypeSpec.Builder,
//        element: TypeElement,
//        fragmentAnnotation: ActivityAnnotation,
//    ) {
//        val factoryClassName =
//            ClassName.bestGuess("com.x930073498.router.impl.FragmentActionDelegate.Factory")
//                .parameterizedBy(element.asClassName())
//        val factory = FunSpec.builder("factory")
//            .addModifiers(KModifier.OVERRIDE)
//            .addStatement("return %T{_,_,bundle->%T().apply{arguments=bundle}}",
//                factoryClassName,
//                element.asType())
//        typeSpec.addFunction(factory.build())
//
//    }




}