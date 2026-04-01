import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.nativeCocoaPods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = ProjectConfiguration.PassageCore.namespace
    compileSdk = ProjectConfiguration.PassageCore.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.PassageCore.minSDK

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "passage-core"
            isStatic = true
        }
    }

    cocoapods {
        ios.deploymentTarget = ProjectConfiguration.iOS.deploymentTarget

        pod("GoogleSignIn")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.android.annotations)

            // Tweener
            implementation(libs.kmpkit)

            // Coroutines
            implementation(libs.kotlin.coroutines.core)

//            // Firebase
//            implementation(libs.firebase.auth)

            // Compose
            implementation(compose.foundation)

        }

        androidMain.dependencies {
            // Coroutines
            implementation(libs.kotlin.coroutines.android)

            // Android
            implementation(libs.android.core)

            // Google Sign In
            implementation(libs.bundles.googleSignIn)
            implementation(libs.android.activity.compose)
        }

        iosMain.dependencies {

        }
    }
}

// region Publishing

group = ProjectConfiguration.PassageCore.Maven.group
version = ProjectConfiguration.PassageCore.versionName

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only disable signing if the flag is explicitly set to false
    val signAllPublicationsProperty = findProperty("mavenPublishing.signAllPublications")
    if (signAllPublicationsProperty == null || signAllPublicationsProperty.toString().toBoolean()) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = ProjectConfiguration.PassageCore.Maven.artifactId.lowercase(), version = version.toString())
    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    pom {
        name = ProjectConfiguration.PassageCore.Maven.name
        description = ProjectConfiguration.PassageCore.Maven.description
        url = ProjectConfiguration.PassageCore.Maven.packageUrl

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        issueManagement {
            system = "GitHub Issues"
            url = "${ProjectConfiguration.PassageCore.Maven.packageUrl}/issues"
        }

        developers {
            developer {
                id = ProjectConfiguration.PassageCore.Maven.Developer.id
                name = ProjectConfiguration.PassageCore.Maven.Developer.name
                email = ProjectConfiguration.PassageCore.Maven.Developer.email
            }
        }

        contributors {
            contributor {
                name = ProjectConfiguration.PassageCore.Maven.Contributor.name
                email = ProjectConfiguration.PassageCore.Maven.Contributor.email
                properties.set(
                    mapOf(
                        "id" to ProjectConfiguration.PassageCore.Maven.Contributor.id
                    )
                )
            }
        }

        scm {
            connection = "scm:git:git://${ProjectConfiguration.PassageCore.Maven.gitUrl}"
            developerConnection = "scm:git:ssh://${ProjectConfiguration.PassageCore.Maven.gitUrl}"
            url = ProjectConfiguration.PassageCore.Maven.packageUrl
        }
    }
}

// endregion Publishing
