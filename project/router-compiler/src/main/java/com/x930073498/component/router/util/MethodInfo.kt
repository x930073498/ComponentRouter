package com.x930073498.component.router.util

import com.squareup.kotlinpoet.*

class MethodInfo(
    private val typeInfo: TypeInfo,
    private val methodMemberName: MemberName? = null,
    private val parameters: List<ParameterInfo> = arrayListOf()
) : Generator {
    private var generator: Generator? = null

    init {
        if (typeInfo.getGenerator() == typeInfo.processor.emptyTypeInfo) generator =
            typeInfo.processor.emptyTypeInfo
    }

    private fun generateInvokerFunction(funSpec: FunSpec.Builder) {
        if (methodMemberName == null) return
        val parameter = parameters.foldIndexed(StringBuilder()) { index, builder, it ->
            it.generateFunctionCode(funSpec)
            builder.append(it.name).apply {
                if (index < parameters.size - 1) append(",")
            }
        }
        funSpec.addStatement("return %M(%L)", methodMemberName, parameter)
    }

    private fun generateInvoker(typeSpec: TypeSpec.Builder) {
        typeSpec.addFunction(FunSpec.builder("invoke")
            .returns(ANY.copy(nullable = true))
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .apply {
                generateInvokerFunction(this)
            }
            .build())

    }

    fun getGenerator(): Generator {
        return generator ?: this
    }

    override fun generate() {
        if (typeInfo is TypeInfo.Empty) return
        val invokerSpecBuilder = TypeSpec.classBuilder(
            ClassName(
                typeInfo.packageName,
                "${typeInfo.classPrefixName}MethodInvoker"
            )
        )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("contextHolder", CONTEXT_HOLDER_NAME)
                            .build()
                    )
                    .addParameter(
                        ParameterSpec.builder("bundle", BUNDLE_NAME)
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("contextHolder", CONTEXT_HOLDER_NAME)
                    .initializer("contextHolder")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("bundle", BUNDLE_NAME)
                    .initializer("bundle")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addSuperinterface(MethodConstants.METHOD_INVOKER_NAME)
        generateInvoker(invokerSpecBuilder)
        val invokerTypeSpec = invokerSpecBuilder.build();

        typeInfo.type = invokerTypeSpec
        typeInfo.generate()
        FileSpec.get(typeInfo.packageName, invokerTypeSpec).writeTo(typeInfo.processor.filer)
    }


}

class ParameterInfo(
    val name: String,
    private val methodBindName: String,
    private val typeName: TypeName,
    private val nullable: Boolean,
    private val isContext: Boolean,
) {
    fun generateFunctionCode(funSpec: FunSpec.Builder) {
        if (isContext) {
            funSpec.addStatement(
                "val %L:%T=contextHolder.getContext()", name, CONTEXT_NAME
            )
            return
        } else {
            funSpec.addStatement(
                "val %L:%T =%T.%L(bundle,%S)",
                name,
                typeName.copy(nullable = true),
                PARAMETER_SUPPORT_NAME,
                methodBindName,
                name,
            )
        }
        if (!nullable) {
            funSpec.addStatement("if(%L==null) return null", name)
        }
    }

}