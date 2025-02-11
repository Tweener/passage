import org.gradle.api.JavaVersion

/**
 * @author Vivien Mahe
 * @since 23/07/2022
 */

object ProjectConfiguration {

    object Passage {
        const val packageName = "com.tweener.passage"
        const val versionName = "1.1.2"
        const val namespace = "$packageName.android"
        const val compileSDK = 35
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

    object Compiler {
        val javaCompatibility = JavaVersion.VERSION_21
        val jvmTarget = javaCompatibility.toString()
    }

    object iOS {
        const val deploymentTarget = "12.0"
    }
}
