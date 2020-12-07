package com.x930073498.util;


public class ComponentConstants {

    private ComponentConstants() {
    }

    public static final String METHOD_ROUTER_ANNOTATION_NAME = "com.x930073498.annotations.MethodAnnotation";
    public static final String SERVICE_ROUTER_ANNOTATION_NAME = "com.x930073498.annotations.ServiceAnnotation";
    public static final String FRAGMENT_ROUTER_ANNOTATION_NAME = "com.x930073498.annotations.FragmentAnnotation";
    public static final String ACTIVITY_ROUTER_ANNOTATION_NAME = "com.x930073498.annotations.ActivityAnnotation";
    public static final String INTERCEPTOR_ROUTER_ANNOTATION_NAME = "com.x930073498.annotations.InterceptorAnnotation";


    public static final String ROUTER_ACTION_PACKAGE_NAME = "com.x930073498.router.action";
    public static final String ROUTER_INTERFACE_PACKAGE_NAME = "com.x930073498.router.impl";
    public static final String CONTEXT_HOLDER_NAME = "ContextHolder";
    public static final String FRAGMENT_TARGET_NAME = "FragmentTarget";
    public static final String ACTIVITY_TARGET_NAME = "ActivityTarget";
    public static final String SERVICE_TARGET_NAME = "ServiceTarget";
    public static final String INTERCEPTOR_TARGET_NAME = "InterceptorTarget";
    public static final String METHOD_TARGET_NAME = "MethodTarget";
    public static final String FRAGMENT_ACTION_DELEGATE = "FragmentActionDelegate";
    public static final String ACTIVITY_ACTION_DELEGATE = "ActivityActionDelegate";
    public static final String SERVICE_ACTION_DELEGATE = "ServiceActionDelegate";
    public static final String INTERCEPTOR_ACTION_DELEGATE = "InterceptorActionDelegate";
    public static final String METHOD_ACTION_DELEGATE = "MethodActionDelegate";
    public static final String FACTORY_NAME = "Factory";
    public static final String SERVICE_NAME = "IService";
    public static final String METHOD_INVOKER_NAME = "MethodInvoker";
    public static final String AUTO_ACTION_NAME = "AutoAction";
    public static final String TARGET_NAME = "Target";
    public static final String AUTO_INTERFACE_NAME = "com.zx.common.auto.IAuto";

    public static final String PARAMETER_SUPPORT_CLASS_NAME = "com.x930073498.router.util.ParameterSupport";

    public static final String ANDROID_PARCELABLE = "android.os.Parcelable";
    public static final String ANDROID_CONTEXT = "android.content.Context";
    public static final String ANDROID_FRAGMENT = "androidx.fragment.app.Fragment";
    public static final String ANDROID_ACTIVITY = "android.app.Activity";
    public static final String ANDROID_BUNDLE = "android.os.Bundle";
    public static final String ANDROID_SPARSEARRAY = "android.util.SparseArray";


    public static final String JAVA_STRING = "java.lang.String";
    public static final String JAVA_INTEGER = "java.lang.Integer";
    public static final String JAVA_ARRAYLIST = "java.util.ArrayList";
    public static final String JAVA_SERIALIZABLE = "java.io.Serializable";
    public static final String JAVA_CHARSEQUENCE = "java.lang.CharSequence";


    public static final String SEPARATOR = "/";


    public static final String HOST_REGEX =
            "^([a-z]|[A-Z])([a-z]|[A-Z]|[0-9])*$";

}
