package com.x930073498.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.asClassName

val AUTO_ACTION_NAME =
    ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME, ComponentConstants.AUTO_ACTION_NAME)
val I_AUTO_NAME = ClassName.bestGuess(ComponentConstants.AUTO_INTERFACE_NAME)
val PARAMETER_SUPPORT_NAME = ClassName.bestGuess(ComponentConstants.PARAMETER_SUPPORT_CLASS_NAME)
val BUNDLE_NAME = ClassName.bestGuess(ComponentConstants.ANDROID_BUNDLE)
val CLASS_STAR_NAME = Class::class.asClassName().parameterizedBy(STAR)
val CONTEXT_HOLDER_NAME = ClassName(ComponentConstants.ROUTER_ACTION_PACKAGE_NAME, ComponentConstants.CONTEXT_HOLDER_NAME)
val CONTEXT_NAME = ClassName.bestGuess(ComponentConstants.ANDROID_CONTEXT)

object FragmentConstants {
    val FRAGMENT_ACTION_DELEGATE_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.FRAGMENT_ACTION_DELEGATE
        )
    val FRAGMENT_ACTION_DELEGATE_FACTORY_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.FRAGMENT_ACTION_DELEGATE,
            ComponentConstants.FACTORY_NAME
        )
    val FRAGMENT_NAME = ClassName.bestGuess(ComponentConstants.ANDROID_FRAGMENT)
    val FRAGMENT_TARGET_NAME = ClassName(
        ComponentConstants.ROUTER_ACTION_PACKAGE_NAME,
        ComponentConstants.TARGET_NAME,
        ComponentConstants.FRAGMENT_TARGET_NAME
    )
}
object ActivityConstants{
    val ACTIVITY_ACTION_DELEGATE_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.ACTIVITY_ACTION_DELEGATE
        )
    val ACTIVITY_NAME = ClassName.bestGuess(ComponentConstants.ANDROID_ACTIVITY)
    val ACTIVITY_TARGET_NAME = ClassName(
        ComponentConstants.ROUTER_ACTION_PACKAGE_NAME,
        ComponentConstants.TARGET_NAME,
        ComponentConstants.ACTIVITY_TARGET_NAME
    )
}
object ServiceConstants{
    val SERVICE_ACTION_DELEGATE_FACTORY_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.SERVICE_ACTION_DELEGATE,
            ComponentConstants.FACTORY_NAME
        )
    val SERVICE_ACTION_DELEGATE_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.SERVICE_ACTION_DELEGATE
        )
    val SERVICE_NAME = ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,ComponentConstants.SERVICE_NAME)
    val SERVICE_TARGET_NAME = ClassName(
        ComponentConstants.ROUTER_ACTION_PACKAGE_NAME,
        ComponentConstants.TARGET_NAME,
        ComponentConstants.SERVICE_TARGET_NAME
    )
}
object MethodConstants{
    val METHOD_ACTION_DELEGATE_FACTORY_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.METHOD_ACTION_DELEGATE,
            ComponentConstants.FACTORY_NAME
        )
    val METHOD_ACTION_DELEGATE_NAME =
        ClassName(
            ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,
            ComponentConstants.METHOD_ACTION_DELEGATE
        )
    val METHOD_INVOKER_NAME=ClassName(ComponentConstants.ROUTER_INTERFACE_PACKAGE_NAME,ComponentConstants.METHOD_INVOKER_NAME)
    val METHOD_TARGET_NAME = ClassName(
        ComponentConstants.ROUTER_ACTION_PACKAGE_NAME,
        ComponentConstants.TARGET_NAME,
        ComponentConstants.METHOD_TARGET_NAME
    )
}