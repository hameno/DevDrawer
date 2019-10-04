object Versions {
    // General
    const val kotlin = "1.3.50"

    // Plugins
    const val androidGradle = "3.5.1"
    const val gradleVersions = "0.25.0"
    const val googleServicesVersion = "4.3.2"
    const val fabricPlugin = "1.31.1"

    // Libs
    const val androidXAppCompat = "1.1.0"
    const val androidXExtensions = "1.1.0"
    const val androidXPreference = "1.1.0"
    const val androidXRoom = "2.1.0"
    const val androidJob = "1.4.1"
    const val attribouter = "0.1.4"
    const val crashlytics = "2.10.1"
    const val firebaseAnalytics = "17.2.0"
    const val kotlinLogging = "1.7.6"
    const val leakCanary = "1.6.3"
    const val rxjava2 = "2.2.13"
    const val rxandroid = "2.1.1"
    const val rxkotlin = "2.4.0"
    const val slf4jAndroidLogger = "1.0.5"

    // Testing
    const val junit = "4.12"
    const val robolectric = "4.3"
    const val assertj = "3.13.2"
    const val mockito = "3.1.0"
}

object Config {
    const val min_sdk = 21
    const val target_sdk = 29
    const val compile_sdk = 29
    const val build_tools = "29.0.2"
}

object Plugins {
    const val android_gradle = "com.android.tools.build:gradle:${Versions.androidGradle}"
    const val kotlin_gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val versions_gradle = "com.github.ben-manes:gradle-versions-plugin:${Versions.gradleVersions}"
    const val google_services_gradle = "com.google.gms:google-services:${Versions.googleServicesVersion}"
    const val fabric = "io.fabric.tools:gradle:${Versions.fabricPlugin}"
}

object Libs {
    // Android Architecture Libraries
    const val room_runtime = "androidx.room:room-runtime:${Versions.androidXRoom}"
    const val room_rxjava2 = "androidx.room:room-rxjava2:${Versions.androidXRoom}"
    const val room_compiler = "androidx.room:room-compiler:${Versions.androidXRoom}"

    // Android Support Library
    const val support_appcompat_v7 = "androidx.appcompat:appcompat:${Versions.androidXAppCompat}"
    const val support_preference_v7 = "androidx.preference:preference:${Versions.androidXPreference}"

    // Android Kotlin Extensions
    const val android_ktx = "androidx.core:core-ktx:${Versions.androidXExtensions}"

    // Android-Job
    const val android_job = "com.evernote:android-job:${Versions.androidJob}"

    // Attribouter
    const val attribouter = "me.jfenn:attribouter:${Versions.attribouter}"

    // Crashlytics
    const val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}"

    // Firebase
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics:${Versions.firebaseAnalytics}"

    // Kotlin
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

    // LeakCanary
    const val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    const val leak_canary_no_op = "com.squareup.leakcanary:leakcanary-android-no-op:${Versions.leakCanary}"

    // Logging
    const val slf4j_android_logger = "de.psdev.slf4j-android-logger:slf4j-android-logger:${Versions.slf4jAndroidLogger}"
    const val kotlin_logging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"

    // RxJava
    const val rxjava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxjava2}"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"
    const val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"

    // Testing
    const val junit = "junit:junit:${Versions.junit}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val assertj = "org.assertj:assertj-core:${Versions.assertj}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
}