package com.x930073498.component.router.compiler

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.google.auto.service.AutoService
import com.x930073498.component.router.util.ComponentConstants
import java.io.Writer
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedAnnotationTypes(
    ComponentConstants.ACTIVITY_ROUTER_ANNOTATION_NAME,
    ComponentConstants.SERVICE_ROUTER_ANNOTATION_NAME,
    ComponentConstants.METHOD_ROUTER_ANNOTATION_NAME,
    ComponentConstants.FRAGMENT_ROUTER_ANNOTATION_NAME,
    ComponentConstants.INTERCEPTOR_ROUTER_ANNOTATION_NAME,
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RouterAnnotationProcessor : BaseProcessor() {

    private var writer: Writer? = null
    private var isWriterClosed = false

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        if (isDocEnable) {
            runCatching {
                writer = filer.createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    "router-map-of-$projectName.json"
                ).openWriter()
            }.onFailure { it.printStackTrace() }
        }
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        annotations?.forEach { annotation ->
            roundEnv?.getElementsAnnotatedWith(annotation)?.forEach {
                generate(it)
            }
        }
        if (docList.isNotEmpty() || !isWriterClosed) {
            runCatching {
                writer?.use {
                    it.append(JSON.toJSONString(docList, SerializerFeature.PrettyFormat))
                }
            }.onFailure {
             messager.printMessage(Diagnostic.Kind.WARNING,"$it")
            }
            isWriterClosed = true
        }
        return false
    }
}