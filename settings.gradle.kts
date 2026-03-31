pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Passage"
include(":passage")
include(":sample:shared")
include(":sample:androidApp")
include(":passage-core")
include(":auth-firebase")
include(":auth-supabase")
