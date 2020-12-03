package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmWildcard
import com.x930073498.annotations.MethodAnnotation
import com.x930073498.annotations.MethodBundleNameAnnotation
import com.x930073498.util.ComponentConstants
import com.x930073498.util.ComponentConstants.ROUTER_ACTION_PACKAGE_NAME
import com.x930073498.util.ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME
import com.x930073498.util.getGroupFromPath
import com.x930073498.util.getParameterMethodName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.coroutines.Continuation
import kotlin.reflect.typeOf

@AutoService(Processor::class)
@SupportedAnnotationTypes("com.x930073498.annotations.MethodAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MethodAnnotationProcessor : BaseProcessor() {
    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        val routeElements =
            roundEnvironment.getElementsAnnotatedWith(MethodAnnotation::class.java)
        if (routeElements.isEmpty()) {
            return false
        }
        messager.printMessage(Diagnostic.Kind.OTHER, "开始生成方法")
        routeElements.forEach {
            createFile(it as ExecutableElement, it.getAnnotation(MethodAnnotation::class.java))
        }
        return false
    }


    private fun createFile(e: ExecutableElement, annotation: MethodAnnotation) {
        messager.printMessage(Diagnostic.Kind.OTHER, "testMethod=${annotation}")

        val path = annotation.path
        var group = annotation.group
        if (group.isEmpty()) {
            group = getGroupFromPath(path) ?: ""
        }
        val pathName = path.replace("/", "$$")
        val type = e.enclosingElement
        val typeName = type.simpleName
        val packageName = elements.getPackageOf(e).qualifiedName.toString()
        val methodName = e.simpleName
        val returnType = e.returnType
        messager.printMessage(Diagnostic.Kind.WARNING, "ExecutableElement=$e")
        val fileName = "${typeName}_$$${methodName}_$$${group}_$$${pathName}"
        val holder = ReturnTypeHolder()
        val invokerType = createMethodInvoker(
            e,
            fileName,
            packageName,
            methodName.toString(),
            returnType,
            holder
        )
        FileSpec.builder(
            packageName,
            fileName
        ).addType(invokerType)
            .addType(
                createMethodActionDelegate(
                    fileName,
                    invokerType,
                    group,
                    path,
                    holder
                )
            )
            .build().writeTo(filer)

    }


    class InvokeParameter(
        val name: String,
        val bundleMethodName: String,
        val type: TypeMirror,
        val typeName: TypeName,
        val nullable: Boolean,
        val isContext: Boolean
    )

    private fun createMethodInvoker(
        e: ExecutableElement,
        fleName: String,
        packageName: String,
        methodName: String,
        returnType: TypeMirror,
        holder: ReturnTypeHolder
    ): TypeSpec {
        val methodMember = MemberName(packageName, methodName)
        var returnTypeName = returnType.javaToKotlinType()
        val returnNullString = "return null"

        fun FunSpec.Builder.inject(): FunSpec.Builder {
            return apply {
                val parameters = e.parameters.mapIndexedNotNull { index, it ->
                    val type = it.asType()
                    val typeName = type.asTypeName().javaToKotlinType()
                    val isNullable = it.getAnnotation(Nullable::class.java) != null
                    messager.printMessage(Diagnostic.Kind.WARNING, "testTypeName=$typeName")
                    if (typeName is ParameterizedTypeName) {
                        if (typeName.rawType == Continuation::class.asTypeName() && index == e.parameters.size - 1) {
                            returnTypeName = typeName.typeArguments[0].javaToKotlinType().let {
                                if (it is WildcardTypeName) {
                                    it.inTypes[0].javaToKotlinType()
                                } else it
                            }
                            return@mapIndexedNotNull null
                        }
                    }
                    val methodBundleNameAnnotation =
                        it.getAnnotation(MethodBundleNameAnnotation::class.java)
                    val parameterMethodName = getParameterMethodName(it)
                    val isContext = types.isSubtype(type, contextTypeMirror)
                    InvokeParameter(
                        methodBundleNameAnnotation?.name ?: it.simpleName.toString(),
                        parameterMethodName,
                        type,
                        typeName,
                        isNullable,
                        isContext
                    )

                }
                parameters.forEach {
                    if (it.isContext) {
                        addStatement(
                            "val %L:%T=contextHolder.getContext()",
                            it.name,
                            contextTypeMirror
                        )
                    } else {
                        addStatement(
                            "val %L:%T =%T.%L(bundle,%S)",
                            it.name,
                            it.typeName.copy(nullable = true),
                            parameterSupportTypeMirror,
                            it.bundleMethodName,
                            it.name
                        )
                        if (!it.nullable) {
                            addStatement("if(%L==null) %L", it.name, returnNullString)
                        }
                    }
                }
                addStatement(
                    "return %M(%L)",
                    methodMember,
                    parameters.foldIndexed(StringBuilder()) { index: Int, acc: StringBuilder, invokeParameter: InvokeParameter ->
                        acc.apply {
                            append(invokeParameter.name)
                            if (index < parameters.size - 1)
                                append(",")
                        }
                    }.toString()
                )


            }.also {
                holder.typeName = returnTypeName
            }
        }
        return TypeSpec.classBuilder(ClassName(fleName, "_$\$MethodInvoker"))
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                    .addParameter(
                        ParameterSpec.builder(
                            "contextHolder",
                            ClassName(ROUTER_ACTION_PACKAGE_NAME, "ContextHolder")
                        ).build()
                    )
                    .addParameter(
                        ParameterSpec.builder("bundle", ClassName("android.os", "Bundle")).build()
                    )
                    .inject()
                    .returns(ANY.copy(nullable = true))
                    .build()
            )
            .addSuperinterface(
                ClassName(
                    ROUTER_INTERFACE_PACKAGE_NAME,
                    "MethodInvoker"
                )
            )
            .build()

    }

    private fun createMethodActionDelegate(
        fileName: String,
        methodInvokerSpec: TypeSpec,
        group: String,
        path: String,
        holder: ReturnTypeHolder
    ): TypeSpec {
        val returnType = holder.typeName ?: ANY
        val methodInvokerInterfaceName = ClassName(
            ROUTER_INTERFACE_PACKAGE_NAME,
            "MethodInvoker"
        )

        return TypeSpec.classBuilder(ClassName(fileName, "_$\$MethodInvokerActionDelegate"))
            .addFunction(
                FunSpec.builder("factory")
                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                    .addStatement(
                        "return %L", TypeSpec.anonymousClassBuilder()
                            .addSuperinterface(
                                ClassName(
                                    ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
                                    "MethodActionDelegate.Factory"
                                )
                            )
                            .addFunction(
                                FunSpec.builder("create")
                                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                                    .addParameter(
                                        "contextHolder",
                                        ClassName(
                                            ROUTER_ACTION_PACKAGE_NAME,
                                            "ContextHolder"
                                        )
                                    )
                                    .addParameter(
                                        "clazz",

                                        Class::class.asClassName()
                                            .parameterizedBy(STAR)
                                    )
                                    .addParameter(
                                        "bundle",
                                        ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)
                                    )
                                    .addStatement("return %N()", methodInvokerSpec)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("target")
                    .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                    .addStatement(
                        "return %T(%T::class.java,%N::class.java)",
                        ClassName(ROUTER_ACTION_PACKAGE_NAME, "Target", "MethodTarget"),
                        returnType,
                        methodInvokerSpec
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("path", String::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .mutable(false)
                    .initializer("%S", path)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("group", String::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .mutable(false)
                    .initializer("%S", group)
                    .build()
            )
            .addSuperinterface(
                ClassName(ROUTER_INTERFACE_PACKAGE_NAME, "MethodActionDelegate")
            )
            .addSuperinterface(ClassName.bestGuess(ComponentConstants.AUTO_INTERFACE_NAME))
            .superclass(
                ClassName(
                    ROUTER_INTERFACE_PACKAGE_NAME,
                    "AutoAction"
                )
            )
            .build()
    }

    data class ReturnTypeHolder(var typeName: TypeName? = null)

}