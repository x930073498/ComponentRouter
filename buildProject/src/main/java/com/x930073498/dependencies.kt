@file:Suppress("SpellCheckingInspection")

package com.x930073498


object Versions {
    const val compileSdk = 29
    const val gradlePlugin = "4.0.0"
    const val kotlin = "1.4.20"
    const val minSdk = 21
    const val targetSdk = 29
    const val core = "1.2.0"
    const val versionCode = 1
    const val versionName = "1.0.0"
}


const val lifecycle_version = "2.3.0"
//const val arch_version = "2.1.0"

object Libraries {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val androidx_core_ktx = "androidx.core:core-ktx:1.3.1"
    const val multidex = "androidx.multidex:multidex:2.0.1"
    const val androidx_appcompat = "androidx.appcompat:appcompat:1.2.0"
    const val androidx_annotation = "androidx.annotation:annotation:1.1.0"
    const val androidx_fragment_ktx = "androidx.fragment:fragment-ktx:1.3.0-beta01"
    const val androidx_fragment = "androidx.fragment:fragment:1.3.0-beta01"
    const val kotlinx_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:1.4.21"
    const val kotlin_compiler_embeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.21"
    const val google_auto_service = "com.google.auto.service:auto-service:1.0-rc7"
    const val kotlin_poet = "com.squareup:kotlinpoet:1.7.2"
    const val room_runtime = "androidx.room:room-runtime:2.2.6"
    const val room_compiler = "androidx.room:room-compiler:2.2.6"
    const val room_ktx = "androidx.room:room-ktx:2.2.6"
    const val moshi = "com.squareup.moshi:moshi:1.11.0"
    const val gson = "com.google.code.gson:gson:2.8.6"
    const val fastJson = "com.alibaba:fastjson:1.1.72.android"
    const val moshi_codegen = "com.squareup.moshi:moshi-kotlin-codegen:1.11.0"
    const val hilt_android = "com.google.dagger:hilt-android:+"
    const val hilt_lifecycle_viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:+"
    const val hilt_android_compiler = "com.google.dagger:hilt-android-compiler:+"
    const val hilt_compiler = "androidx.hilt:hilt-compiler:+"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    const val kotlinx_serialization_json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1"
    const val kotlinx_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1"
    const val datastore_preferences = "androidx.datastore:datastore-preferences:1.0.0-alpha04"
    const val startup = "androidx.startup:startup-runtime:1.0.0"
    const val agent_web = "com.just.agentweb:agentweb:4.1.3"
    const val navigation_fragment_ktx = "androidx.navigation:navigation-fragment-ktx:2.3.2"
    const val navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:2.3.2"

    //lifecycle
    const val lifecycle_viewmodel_ktx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    const val lifecycle_livedata_ktx =
        "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    const val lifecycle_runtime_ktx =
        "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
    const val lifecycle_viewmodel_savedstate =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version"
    const val lifecycle_common_java8 =
        "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
    const val lifecycle_service =
        "androidx.lifecycle:lifecycle-service:$lifecycle_version"
    const val lifecycle_process =
        "androidx.lifecycle:lifecycle-process:$lifecycle_version"

    //mvrx
    const val mvrx = "com.airbnb.android:mavericks:2.0.0"

}



