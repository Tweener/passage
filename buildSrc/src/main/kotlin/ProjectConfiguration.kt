import org.gradle.api.JavaVersion

/**
 * @author Vivien Mahe
 * @since 23/07/2022
 */

object ProjectConfiguration {

    object Passage {
        const val packageName = "com.tweener.passage"
        const val versionName = "1.6.0"
        const val namespace = "$packageName.android"
        const val compileSDK = 36
        const val minSDK = 24

        object Maven {
            const val name = "Passage"
            const val description = "A Kotlin/Compose Multiplatform library for seamless authentication on Android and iOS."
            const val group = "io.github.tweener"
            const val packageUrl = "https://github.com/Tweener/passage"
            const val gitUrl = "github.com:Tweener/passage.git"

            object Developer {
                const val id = "Tweener"
                const val name = "Vivien Mahé"
                const val email = "vivien@tweener-labs.com"
            }
        }
    }

    object PassageCore {
        const val packageName = "com.tweener.passage.core"
        const val versionName = "1.6.0"
        const val namespace = "$packageName.android"
        const val compileSDK = 36
        const val minSDK = 24

        object Maven {
            const val name = "Passage Core"
            const val artifactId = "passage-core"
            const val description = "Core module for Passage providing shared domain, contracts, and abstractions."
            const val group = "io.github.tweener"
            const val packageUrl = "https://github.com/Tweener/passage"
            const val gitUrl = "github.com:Tweener/passage.git"

            object Developer {
                const val id = "Tweener"
                const val name = "Vivien Mahé"
                const val email = "vivien@tweener-labs.com"
            }

            object Contributor {
                const val id = "chirag38-unity"
                const val name = "Chirag Redij"
                const val email = "chirag.redij@gmail.com"
            }
        }
    }

    object PassageAuthFirebase {
        const val packageName = "com.tweener.passage.auth.firebase"
        const val versionName = "1.6.0"
        const val namespace = "$packageName.android"
        const val compileSDK = 36
        const val minSDK = 24

        object Maven {
            const val name = "Passage Auth Firebase"
            const val artifactId = "passage-auth-firebase"
            const val description = "Firebase authentication plugin for Passage, enabling seamless Firebase Auth integration."
            const val group = "io.github.tweener"
            const val packageUrl = "https://github.com/Tweener/passage"
            const val gitUrl = "github.com:Tweener/passage.git"

            object Developer {
                const val id = "Tweener"
                const val name = "Vivien Mahé"
                const val email = "vivien@tweener-labs.com"
            }

            object Contributor {
                const val id = "chirag38-unity"
                const val name = "Chirag Redij"
                const val email = "chirag.redij@gmail.com"
            }
        }
    }

    object PassageAuthSupabase {
        const val packageName = "com.tweener.passage.auth.supabase"
        const val versionName = "1.6.0"
        const val namespace = "$packageName.android"
        const val compileSDK = 36
        const val minSDK = 24

        object Maven {
            const val name = "Passage Auth Supabase"
            const val artifactId = "passage-auth-supabase"
            const val description = "Supabase authentication plugin for Passage, enabling seamless Supabase Auth integration."
            const val group = "io.github.tweener"
            const val packageUrl = "https://github.com/Tweener/passage"
            const val gitUrl = "github.com:Tweener/passage.git"

            object Developer {
                const val id = "Tweener"
                const val name = "Vivien Mahé"
                const val email = "vivien@tweener-labs.com"
            }

            object Contributor {
                const val id = "chirag38-unity"
                const val name = "Chirag Redij"
                const val email = "chirag.redij@gmail.com"
            }
        }
    }

    object Compiler {
        val javaCompatibility = JavaVersion.VERSION_21
        val jvmTarget = javaCompatibility.toString()
    }

    object iOS {
        const val deploymentTarget = "12.0"
    }
}
