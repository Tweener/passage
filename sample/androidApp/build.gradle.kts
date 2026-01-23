plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = ProjectConfiguration.Passage.packageName + ".sample"
    compileSdk = ProjectConfiguration.Passage.compileSDK

    defaultConfig {
        applicationId = ProjectConfiguration.Passage.packageName + ".sample"
        minSdk = ProjectConfiguration.Passage.minSDK
        targetSdk = ProjectConfiguration.Passage.compileSDK
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":sample:shared"))

    implementation(libs.android.activity)
    implementation(libs.android.activity.compose)
    implementation(libs.android.startup)
}
