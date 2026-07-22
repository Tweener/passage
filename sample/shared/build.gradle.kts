plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = ProjectConfiguration.Passage.packageName + ".sample.shared"
        compileSdk = ProjectConfiguration.Passage.compileSDK
        minSdk = ProjectConfiguration.Passage.minSDK

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    // region iOS configuration

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true

            // Add here any extra framework dependencies
            export(project(":passage"))
        }
    }

    // endregion iOS configuration

    sourceSets {
        commonMain.dependencies {
            api(project(":passage"))

            // Tweener
            implementation(libs.kmpkit)

            // Compose
            implementation(compose.runtime)
            implementation(libs.compose.multiplatform.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.compose.multiplatform.material3)

            implementation(libs.jetbrains.lifecycle.runtime.compose)
        }

        androidMain.dependencies {
            implementation(libs.android.activity)
            implementation(libs.android.activity.compose)
        }
    }
}
