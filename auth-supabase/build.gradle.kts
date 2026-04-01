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
    namespace = ProjectConfiguration.PassageAuthSupabase.namespace
    compileSdk = ProjectConfiguration.PassageAuthSupabase.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.PassageAuthSupabase.minSDK

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
            baseName = "passage-auth-supabase"
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

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.bundles.supabase)

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

group = ProjectConfiguration.PassageAuthSupabase.Maven.group
version = ProjectConfiguration.PassageAuthSupabase.versionName

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only disable signing if the flag is explicitly set to false
    val signAllPublicationsProperty = findProperty("mavenPublishing.signAllPublications")
    if (signAllPublicationsProperty == null || signAllPublicationsProperty.toString().toBoolean()) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = ProjectConfiguration.PassageAuthSupabase.Maven.artifactId.lowercase(), version = version.toString())
    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        )
    )

    pom {
        name = ProjectConfiguration.PassageAuthSupabase.Maven.name
        description = ProjectConfiguration.PassageAuthSupabase.Maven.description
        url = ProjectConfiguration.PassageAuthSupabase.Maven.packageUrl

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        issueManagement {
            system = "GitHub Issues"
            url = "${ProjectConfiguration.PassageAuthSupabase.Maven.packageUrl}/issues"
        }

        developers {
            developer {
                id = ProjectConfiguration.PassageAuthSupabase.Maven.Developer.id
                name = ProjectConfiguration.PassageAuthSupabase.Maven.Developer.name
                email = ProjectConfiguration.PassageAuthSupabase.Maven.Developer.email
            }
        }

        contributors {
            contributor {
                name = ProjectConfiguration.PassageAuthSupabase.Maven.Contributor.name
                email = ProjectConfiguration.PassageAuthSupabase.Maven.Contributor.email
                properties.set(
                    mapOf(
                        "id" to ProjectConfiguration.PassageAuthSupabase.Maven.Contributor.id
                    )
                )
            }
        }

        scm {
            connection = "scm:git:git://${ProjectConfiguration.PassageAuthSupabase.Maven.gitUrl}"
            developerConnection = "scm:git:ssh://${ProjectConfiguration.PassageAuthSupabase.Maven.gitUrl}"
            url = ProjectConfiguration.PassageAuthSupabase.Maven.packageUrl
        }
    }
}

// endregion Publishing
