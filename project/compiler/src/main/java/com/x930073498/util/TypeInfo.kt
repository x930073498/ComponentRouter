package com.x930073498.util

import com.squareup.kotlinpoet.*
import com.x930073498.annotations.ValueAutowiredAnnotation
import com.x930073498.compiler.BaseProcessor
import java.lang.StringBuilder
import javax.lang.model.element.Element
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic


interface Generator {
    fun generate()
}

sealed class TypeInfo constructor(
    val processor: BaseProcessor,
    private val path: String,//path
    private val group: String,//group
    val classPrefixName: String,//生成类的前缀
    private val className: String,//生成类名字
    val packageName: String,//元素的包名
    var type: Any,//元素的Type
    private val superClassName: TypeName,//父类
    private val supperInterfaces: List<TypeName>,//父接口
    private val injectTargetTypeName: TypeName?,//注入方法中注入属性的目标类
    val factoryTypeName: TypeName?,//factory类
    private val element: Element? = null,//元素
    private val parentPath: String,
    private val interceptors: Array<String> = arrayOf(),
    val autoInjectList: MutableList<ValueAutowired> = arrayListOf()//属性注入元素信息列表
) : Generator {
    private var _delegateGenerator: Generator? = null


    protected fun setGenerator(generator: Generator) {
        _delegateGenerator = generator
    }

    fun getGenerator(): Generator {
        return _delegateGenerator ?: this
    }

    class Empty(processor: BaseProcessor) : TypeInfo(
        processor, "", "", "", "", "", ANY, ANY, emptyList(), null, null, parentPath = ""
    ) {
        override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        }

        override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
        }

        override fun generate() {

        }


    }

    init {
        if (paths.contains(path)) {
            setGenerator(processor.emptyTypeInfo)
            processor.messager.printMessage(Diagnostic.Kind.WARNING, "路径冲突\n${toString()}")
        } else {
            paths.add(path)
        }
    }

    companion object {
        val paths = arrayListOf<String>()
    }

    private fun generateSuper(typeSpec: TypeSpec.Builder) {
        typeSpec.addSuperinterfaces(supperInterfaces)
        typeSpec.superclass(superClassName)
    }

    private fun generatePathCode(typeSpec: TypeSpec.Builder) {
        typeSpec.addProperty(
            PropertySpec.builder("path", String::class)
                .addModifiers(KModifier.OVERRIDE)
                .mutable(false)
                .initializer("%S", path)
                .build()
        )
    }

    private fun generateGroupCode(typeSpec: TypeSpec.Builder) {
        typeSpec.addProperty(
            PropertySpec.builder("group", String::class)
                .addModifiers(KModifier.OVERRIDE)
                .mutable(false)
                .initializer("%S", group)
                .build()
        )
    }


    private fun generateInjectCode(typeSpec: TypeSpec.Builder) {
        if (injectTargetTypeName == null) return
        typeSpec.addFunction(
            FunSpec.builder("inject")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec(
                        "bundle",
                        BUNDLE_NAME
                    )
                )
                .addParameter(
                    ParameterSpec.builder(
                        "target",
                        injectTargetTypeName
                    ).build()
                )
//                .addParameter(ParameterSpec.builder("parent",ClassName(packageName,className)).build())
                .addStatement("target as %T", type)
                .apply {
                    autoInjectList.forEach {
                        addStatement(
                            "%L.%N = %T.%L(%N,%S)?:%L.%N",
                            "target",
                            it.elementName,
                            PARAMETER_SUPPORT_NAME,
                            processor.getParameterMethodName(it.element),
                            "bundle",
                            it.name,
                            "target",
                            it.elementName,
                        )
                    }
                }
                .build()
        )
    }

    open fun generateStringCode(typeSpec: TypeSpec.Builder) {
        val target = FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement(
                "return \"path=\$path,group=\$group,targetClass=\${%T::class.java}\"",
                type
            )
        typeSpec.addFunction(target.build())
    }

    open fun generateTargetCode(typeSpec: TypeSpec.Builder) {
        typeSpec.addFunction(
            FunSpec.builder("target")
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .apply { generateTargetReturnCode(this) }
                .build()
        )
    }

    protected abstract fun generateTargetReturnCode(funSpec: FunSpec.Builder)

    protected abstract fun generateFactoryReturnCode(funSpec: FunSpec.Builder)

    private fun generateInterceptorsCode(typeSpec: TypeSpec.Builder) {
        if (interceptors.isEmpty()) return
        val parameters = interceptors.foldIndexed(StringBuilder()) { index, builder, it ->
            builder.apply {
                append(it)
                if (index < interceptors.size - 1) {
                    append(",")
                }
            }
        }.toString()

        val memberName = MemberName("kotlin.collections", "arrayListOf")
        typeSpec.addFunction(
            FunSpec.builder("interceptors")
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return %M(%S)", memberName, parameters)
                .build()
        )
    }

    open fun generateFactoryCode(typeSpec: TypeSpec.Builder) {
        if (factoryTypeName == null) return
        val factoryObject = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(factoryTypeName)
            .addFunction(
                FunSpec.builder("create")
                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                    .addParameter("contextHolder", CONTEXT_HOLDER_NAME)
                    .addParameter("clazz", CLASS_STAR_NAME)
                    .addParameter("bundle", BUNDLE_NAME)
                    .apply {
                        generateFactoryReturnCode(this)
                    }
                    .build()
            )
            .build()
        val factory = FunSpec.builder("factory")
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .addStatement("return %L", factoryObject)
        typeSpec.addFunction(factory.build())
    }

    protected open fun generateOtherCode(typeSpec: TypeSpec.Builder) {

    }
    protected open fun generateParentCode(typeSpec: TypeSpec.Builder){
        if (parentPath.isEmpty())return
        typeSpec.addFunction(FunSpec.builder("parentPath")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %S",parentPath)
            .build())
    }

    protected open fun generate(typeSpec: TypeSpec.Builder) {
        generateSuper(typeSpec)
        generatePathCode(typeSpec)
        generateGroupCode(typeSpec)
        generateInterceptorsCode(typeSpec)
        generateParentCode(typeSpec)
        generateInjectCode(typeSpec)
        generateTargetCode(typeSpec)
        generateOtherCode(typeSpec)
        generateFactoryCode(typeSpec)
        generateStringCode(typeSpec)
    }

    private fun getTypeSpec(): TypeSpec {
        val typeSpec = TypeSpec.classBuilder(
            ClassName(
                packageName,
                className
            )
        )
        generate(typeSpec)
        return typeSpec.build()
    }

    override fun generate() {
        FileSpec.get(packageName, getTypeSpec()).writeTo(processor.filer)
    }

    final override fun toString(): String {
        return "[path=$path,\ngroup=$group,\npackageName=$packageName,\nelement=$element\n]\n"
    }
}

class InterceptorInfo(
    processor: BaseProcessor,
    path: String,
    group: String,
    classPrefixName: String,
    className: String,
    packageName: String,
    type: Any,
    superClassName: TypeName,
    supperInterfaces: List<TypeName>,
    injectTargetTypeName: TypeName?,
    factoryTypeName: TypeName?,
    element: Element? = null,
    interceptors: Array<String> = arrayOf(),
    autoInjectList: MutableList<ValueAutowired> = arrayListOf(),
    parentPath: String
) : TypeInfo(
    processor,
    path,
    group,
    classPrefixName,
    className,
    packageName,
    type,
    superClassName,
    supperInterfaces,
    injectTargetTypeName,
    factoryTypeName,
    element,
    parentPath=parentPath,
    interceptors,
    autoInjectList,
) {
    override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement(
            "return %T(%T::class.java)",
            InterceptorConstants.INTERCEPTOR_TARGET_NAME,
            type
        )
    }

    override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement("return %T()", type)
    }

    override fun generateFactoryCode(typeSpec: TypeSpec.Builder) {
        if (factoryTypeName == null) return
        val factoryObject = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(factoryTypeName)
            .addFunction(
                FunSpec.builder("create")
                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                    .addParameter("contextHolder", CONTEXT_HOLDER_NAME)
                    .addParameter("clazz", CLASS_STAR_NAME)
                    .apply {
                        generateFactoryReturnCode(this)
                    }
                    .build()
            )
            .build()
        val factory = FunSpec.builder("factory")
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .addStatement("return %L", factoryObject)
        typeSpec.addFunction(factory.build())
    }

}

class ServiceInfo(
    processor: BaseProcessor,
    path: String,
    group: String,
    classPrefixName: String,
    className: String,
    packageName: String,
    typeName: TypeName,
    superClassName: TypeName,
    private val isSingleTone: Boolean,
    private val isAutoInvoke: Boolean,
    supperInterfaces: List<TypeName>,
    injectTargetTypeName: TypeName,
    factoryTypeName: TypeName?,
    element: Element? = null,
    interceptors: Array<String>,
    autoInjectList: MutableList<ValueAutowired> = arrayListOf(),
    parentPath: String
) : TypeInfo(
    processor,
    path,
    group,
    classPrefixName,
    className,
    packageName,
    typeName,
    superClassName,
    supperInterfaces,
    injectTargetTypeName,
    factoryTypeName,
    element,
    parentPath=parentPath,
    interceptors,
    autoInjectList
) {
    override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement(
            "return %T(%T::class.java,%L)",
            ServiceConstants.SERVICE_TARGET_NAME,
            type,
            isSingleTone
        )
    }

    override fun generateOtherCode(typeSpec: TypeSpec.Builder) {
        val autoInvoke = FunSpec.builder("autoInvoke")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return %L", isAutoInvoke)
        typeSpec.addFunction(autoInvoke.build())
    }

    override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement("return %T()", type)
    }
}

class ActivityInfo(
    processor: BaseProcessor,
    path: String,
    group: String,
    classPrefixName: String,
    className: String,
    packageName: String,
    typeName: TypeName,
    superClassName: TypeName,
    supperInterfaces: List<TypeName>,
    injectTargetTypeName: TypeName,
    element: Element?,
    interceptors: Array<String>,
    autoInjectList: MutableList<ValueAutowired> = arrayListOf(),
    parentPath: String
) : TypeInfo(
    processor,
    path,
    group,
    classPrefixName,
    className,
    packageName,
    typeName,
    superClassName,
    supperInterfaces,
    injectTargetTypeName,
    factoryTypeName = null,
    element,
    parentPath=parentPath,
    interceptors,
    autoInjectList
) {
    override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement(
            "return %T(%T::class.java)",
            ActivityConstants.ACTIVITY_TARGET_NAME,
            type
        )
    }

    override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
    }

}

class FragmentInfo(
    processor: BaseProcessor,
    path: String,
    group: String,
    classPrefixName: String,
    className: String,
    packageName: String,
    typeName: TypeName,
    superClassName: TypeName,
    supperInterfaces: List<TypeName>,
    injectTargetTypeName: TypeName,
    factoryTypeName: TypeName,
    element: Element? = null,
    interceptors: Array<String>,
    autoInjectList: MutableList<ValueAutowired> = arrayListOf(),
    parentPath: String
) : TypeInfo(
    processor,
    path,
    group,
    classPrefixName,
    className,
    packageName,
    typeName,
    superClassName,
    supperInterfaces,
    injectTargetTypeName,
    factoryTypeName,
    element,
    parentPath=parentPath,
    interceptors,
    autoInjectList
) {

    override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement(
            "return %T(%T::class.java)",
            FragmentConstants.FRAGMENT_TARGET_NAME,
            type
        )
    }

    override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement("return %T().apply{arguments=bundle}", type)
    }

}

class MethodInvokerInfo(
    processor: BaseProcessor,
    path: String,
    group: String,
    classPrefixName: String,
    className: String,
    packageName: String,
    type: Any,
    superClassName: TypeName,
    supperInterfaces: List<TypeName>,
    injectTargetTypeName: TypeName?,
    factoryTypeName: TypeName?,
    element: Element? = null,
    interceptors: Array<String>,
    autoInjectList: MutableList<ValueAutowired> = arrayListOf(),
    parentPath: String,
) : TypeInfo(
    processor,
    path,
    group,
    classPrefixName,
    className,
    packageName,
    type,
    superClassName,
    supperInterfaces,
    injectTargetTypeName,
    factoryTypeName,
    element,
    parentPath=parentPath,
    interceptors,
    autoInjectList
) {
    override fun generateTargetReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement(
            "return %T(%N::class.java)",
            MethodConstants.METHOD_TARGET_NAME,
            type
        )
    }

    override fun generateFactoryReturnCode(funSpec: FunSpec.Builder) {
        funSpec.addStatement("return %N()", type)
    }

    override fun generateStringCode(typeSpec: TypeSpec.Builder) {
        val target = FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement(
                "return \"path=\$path,group=\$group,targetClass=\${%N::class.java}\"",
                type
            )
        typeSpec.addFunction(target.build())
    }


}

data class ValueAutowired(
    val element: VariableElement,
    val annotation: ValueAutowiredAnnotation,
    val name: String,
    val elementName: String,
    val info: TypeInfo
)
