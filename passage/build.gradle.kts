import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.nativeCocoaPods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    id("maven-publish")
    id("signing")
}

android {
    namespace = ProjectConfiguration.Passage.namespace
    compileSdk = ProjectConfiguration.Passage.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Passage.minSDK

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
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "passage"
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

            // Firebase
            implementation(libs.firebase.auth)

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

group = ProjectConfiguration.Passage.Maven.group
version = ProjectConfiguration.Passage.versionName

// Dokka configuration
tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        jdkVersion.set(ProjectConfiguration.Compiler.jvmTarget.toInt())
        languageVersion.set(libs.versions.kotlin)

        sourceLink {
            localDirectory.set(rootProject.projectDir)
            remoteUrl.set(URI(ProjectConfiguration.Passage.Maven.packageUrl + "/tree/main").toURL())
            remoteLineSuffix.set("#L")
        }
    }
}

publishing {
    publications {
        publications.withType<MavenPublication> {
            artifact(tasks["dokkaJavadocJar"])

            pom {
                name.set(ProjectConfiguration.Passage.Maven.name)
                description.set(ProjectConfiguration.Passage.Maven.description)
                url.set(ProjectConfiguration.Passage.Maven.packageUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("${ProjectConfiguration.Passage.Maven.packageUrl}/issues")
                }

                developers {
                    developer {
                        id.set(ProjectConfiguration.Passage.Maven.Developer.id)
                        name.set(ProjectConfiguration.Passage.Maven.Developer.name)
                        email.set(ProjectConfiguration.Passage.Maven.Developer.email)
                    }
                }

                scm {
                    connection.set("scm:git:git://${ProjectConfiguration.Passage.Maven.gitUrl}")
                    developerConnection.set("scm:git:ssh://${ProjectConfiguration.Passage.Maven.gitUrl}")
                    url.set(ProjectConfiguration.Passage.Maven.packageUrl)
                }
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        println("Signing lib...")
        useGpgCmd()
        sign(publishing.publications)
    }
}

// endregion Publishing
