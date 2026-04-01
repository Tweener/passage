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
    namespace = ProjectConfiguration.PassageAuthFirebase.namespace
    compileSdk = ProjectConfiguration.PassageAuthFirebase.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.PassageAuthFirebase.minSDK

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
            baseName = "passage-auth-firebase"
            isStatic = true
        }
    }

    cocoapods {
        ios.deploymentTarget = ProjectConfiguration.iOS.deploymentTarget
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.android.annotations)

            // Tweener
            implementation(libs.kmpkit)

            // Coroutines
            implementation(libs.kotlin.coroutines.core)

            // Firebase
            implementation(libs.firebase.auth)

            api(project(":passage-core"))

        }

        androidMain.dependencies {
            // Coroutines
            implementation(libs.kotlin.coroutines.android)

            // Android
            implementation(libs.android.core)
        }

        iosMain.dependencies {

        }
    }
}

// region Publishing

group = ProjectConfiguration.PassageAuthFirebase.Maven.group
version = ProjectConfiguration.PassageAuthFirebase.versionName

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only disable signing if the flag is explicitly set to false
    val signAllPublicationsProperty = findProperty("mavenPublishing.signAllPublications")
    if (signAllPublicationsProperty == null || signAllPublicationsProperty.toString().toBoolean()) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = ProjectConfiguration.PassageAuthFirebase.Maven.artifactId.lowercase(), version = version.toString())
    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    pom {
        name = ProjectConfiguration.PassageAuthFirebase.Maven.name
        description = ProjectConfiguration.PassageAuthFirebase.Maven.description
        url = ProjectConfiguration.PassageAuthFirebase.Maven.packageUrl

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        issueManagement {
            system = "GitHub Issues"
            url = "${ProjectConfiguration.PassageAuthFirebase.Maven.packageUrl}/issues"
        }

        developers {
            developer {
                id = ProjectConfiguration.PassageAuthFirebase.Maven.Developer.id
                name = ProjectConfiguration.PassageAuthFirebase.Maven.Developer.name
                email = ProjectConfiguration.PassageAuthFirebase.Maven.Developer.email
            }
        }

        contributors {
            contributor {
                name = ProjectConfiguration.PassageAuthFirebase.Maven.Contributor.name
                email = ProjectConfiguration.PassageAuthFirebase.Maven.Contributor.email
                properties.set(
                    mapOf(
                        "id" to ProjectConfiguration.PassageAuthFirebase.Maven.Contributor.id
                    )
                )
            }
        }

        scm {
            connection = "scm:git:git://${ProjectConfiguration.PassageAuthFirebase.Maven.gitUrl}"
            developerConnection = "scm:git:ssh://${ProjectConfiguration.PassageAuthFirebase.Maven.gitUrl}"
            url = ProjectConfiguration.PassageAuthFirebase.Maven.packageUrl
        }
    }
}

// endregion Publishing
