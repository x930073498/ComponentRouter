package com.x930073498.compiler

import com.google.auto.service.AutoService
import com.x930073498.util.ComponentConstants
import com.x930073498.util.generate
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedAnnotationTypes(
    ComponentConstants.ACTIVITY_ROUTER_ANNOTATION_NAME,
    ComponentConstants.SERVICE_ROUTER_ANNOTATION_NAME,
    ComponentConstants.METHOD_ROUTER_ANNOTATION_NAME,
    ComponentConstants.FRAGMENT_ROUTER_ANNOTATION_NAME,
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RouterAnnotationProcessor : BaseProcessor() {
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        annotations?.forEach { annotation ->
            roundEnv?.getElementsAnnotatedWith(annotation)?.forEach {
                generate(it)
            }

        }
        return false
    }
}