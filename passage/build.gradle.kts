import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlinMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.nativeCocoaPods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.maven.publish)
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = ProjectConfiguration.Passage.namespace
        compileSdk = ProjectConfiguration.Passage.compileSDK
        minSdk = ProjectConfiguration.Passage.minSDK

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    listOf(
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
            implementation(libs.compose.multiplatform.foundation)

        }

        androidMain.dependencies {
            // Firebase (BoM pins the transitive com.google.firebase artifact versions
            // that GitLive's firebase-auth leaves unversioned on the compile classpath)
            implementation(project.dependencies.platform(libs.firebase.bom))

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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only disable signing if the flag is explicitly set to false
    val signAllPublicationsProperty = findProperty("mavenPublishing.signAllPublications")
    if (signAllPublicationsProperty == null || signAllPublicationsProperty.toString().toBoolean()) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = ProjectConfiguration.Passage.Maven.name.lowercase(), version = version.toString())
    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
        )
    )

    pom {
        name = ProjectConfiguration.Passage.Maven.name
        description = ProjectConfiguration.Passage.Maven.description
        url = ProjectConfiguration.Passage.Maven.packageUrl

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        issueManagement {
            system = "GitHub Issues"
            url = "${ProjectConfiguration.Passage.Maven.packageUrl}/issues"
        }

        developers {
            developer {
                id = ProjectConfiguration.Passage.Maven.Developer.id
                name = ProjectConfiguration.Passage.Maven.Developer.name
                email = ProjectConfiguration.Passage.Maven.Developer.email
            }
        }

        scm {
            connection = "scm:git:git://${ProjectConfiguration.Passage.Maven.gitUrl}"
            developerConnection = "scm:git:ssh://${ProjectConfiguration.Passage.Maven.gitUrl}"
            url = ProjectConfiguration.Passage.Maven.packageUrl
        }
    }
}

// endregion Publishing
