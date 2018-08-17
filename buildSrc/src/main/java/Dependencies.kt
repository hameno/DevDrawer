object Versions {
    // General
    const val kotlin = "1.2.61"

    // Plugins
    const val androidGradle = "3.2.0-beta05"
    const val gradleVersions = "0.20.0"
    const val googleServicesVersion = "4.0.2"
    const val fabricPlugin = "1.25.4"

    // Libs
    const val androidArchitectureComponentsRoom = "2.0.0-rc01"
    const val androidXAppCompat = "1.0.0-rc01"
    const val androidXPreference = "1.0.0-rc01"
    const val androidKotlinExtensions = "1.0.0-rc01"
    const val androidJob = "1.2.6"
    const val attribouter = "0.1.0"
    const val crashlytics = "2.9.4"
    const val firebaseCore = "16.0.1"
    const val kotlinLogging = "1.5.9"
    const val leakCanary = "1.6.1"
    const val multidexVersion = "2.0.0"
    const val rxjava2 = "2.2.0"
    const val rxandroid = "2.1.0"
    const val rxkotlin = "2.3.0"
    const val slf4jAndroidLogger = "1.0.5"

    // Testing
    const val junit = "4.12"
    const val robolectric = "3.8"
    const val assertj = "3.11.0"
    const val mockito = "2.21.0"
}

object Config {
    const val min_sdk = 16
    const val target_sdk = 28
    const val compile_sdk = 28
    const val build_tools = "28.0.2"
}

object Plugins {
    val android_gradle = "com.android.tools.build:gradle:${Versions.androidGradle}"
    val kotlin_gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val versions_gradle = "com.github.ben-manes:gradle-versions-plugin:${Versions.gradleVersions}"
    val google_services_gradle = "com.google.gms:google-services:${Versions.googleServicesVersion}"
    val fabric = "io.fabric.tools:gradle:${Versions.fabricPlugin}"
}

object Libs {
    // Android Architecture Libraries
    val room_runtime = "androidx.room:room-runtime:${Versions.androidArchitectureComponentsRoom}"
    val room_rxjava2 = "androidx.room:room-rxjava2:${Versions.androidArchitectureComponentsRoom}"
    val room_compiler = "androidx.room:room-compiler:${Versions.androidArchitectureComponentsRoom}"

    // Android Support Library
    val support_appcompat_v7 = "androidx.appcompat:appcompat:${Versions.androidXAppCompat}"
    val support_preference_v7 = "androidx.preference:preference:${Versions.androidXPreference}"

    // Android Kotlin Extensions
    val android_ktx = "androidx.core:core-ktx:${Versions.androidKotlinExtensions}"

    // Android-Job
    val android_job = "com.evernote:android-job:${Versions.androidJob}"

    // Attribouter
    val attribouter = "me.jfenn:attribouter:${Versions.attribouter}"

    // Crashlytics
    val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}"

    // Firebase
    val firebaseCore = "com.google.firebase:firebase-core:${Versions.firebaseCore}"

    // Kotlin
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

    // LeakCanary
    val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val leak_canary_no_op = "com.squareup.leakcanary:leakcanary-android-no-op:${Versions.leakCanary}"

    // Logging
    val slf4j_android_logger = "de.psdev.slf4j-android-logger:slf4j-android-logger:${Versions.slf4jAndroidLogger}"
    val kotlin_logging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"

    // MultiDex
    val multidex = "androidx.multidex:multidex:${Versions.multidexVersion}"
    val multidex_instrumentation = "androidx.multidex:multidex-instrumentation:${Versions.multidexVersion}"

    // RxJava
    val rxjava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxjava2}"
    val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"
    val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"

    // Testing
    val junit = "junit:junit:${Versions.junit}"
    val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    val assertj = "org.assertj:assertj-core:${Versions.assertj}"
    val mockito = "org.mockito:mockito-core:${Versions.mockito}"
}