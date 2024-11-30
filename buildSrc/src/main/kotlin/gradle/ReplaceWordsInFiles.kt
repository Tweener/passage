package gradle

import gradle.RenameProjectTask.Companion.ACTUAL_MODULE_NAME
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class ReplaceWordsInFiles {

    companion object {
        private const val GITHUB_WORKFLOW_BUILD_FILENAME = "buildRelease.yml"
        private const val GITHUB_WORKFLOW_NOTIFY_FILENAME = "notify.yml"
        private const val LIBRARY_BUILD_GRADLE_FILENAME = "build.gradle.kts"
        private const val SETTINGS_FILENAME = "settings.gradle.kts"
        private const val PROJECT_CONFIG_FILENAME = "ProjectConfiguration.kt"

        private const val THIS_TASK_FILENAME = "RenameProjectTask.kt"
        private const val RENAME_DIRECTORIES_FILENAME = "RenameDirectories.kt"
        private const val REPLACE_WORDS_IN_FILES_FILENAME = "ReplaceWordsInFiles.kt"
        private const val DELETE_EXISTING_MODULE_FILENAME = "DeleteExistingModuleDirectory.kt"

        private const val ACTUAL_PROJECT_NAME = "Kmplate_Library"
        private const val ACTUAL_PROJECT_CONFIGURATION_OBJECT = "MyProject" // For ProjectConfiguration.kt
        private const val ACTUAL_PACKAGE_NAME_CONFIGURATION_OBJECT = "com.tweener.changehere" // For ProjectConfiguration.kt

        private const val BUILD_SRC_DIR = "buildSrc"
        private const val BUILD_DIR = "build"
    }

    fun execute(projectDir: File, projectName: String, moduleName: String, packageName: String, dryRun: Boolean, updatedFiles: MutableList<String>) {
        println("\n--- Updating words in files ---")

        // Find all .kt files and specified configuration files
        val specificFilesToUpdate = listOf(GITHUB_WORKFLOW_BUILD_FILENAME, GITHUB_WORKFLOW_NOTIFY_FILENAME, SETTINGS_FILENAME, PROJECT_CONFIG_FILENAME)

        // Files to exclude
        val filesToExclude = listOf(THIS_TASK_FILENAME, RENAME_DIRECTORIES_FILENAME, REPLACE_WORDS_IN_FILES_FILENAME, DELETE_EXISTING_MODULE_FILENAME)

        val filesToUpdate = projectDir
            .walkTopDown()
            .filter { file -> !file.isInsideBuildDirectory() } // Exclude files inside "build"
            .filter { it.isFile && (it.extension == "kt" || it.name in specificFilesToUpdate || it.path.endsWith(LIBRARY_BUILD_GRADLE_FILENAME)) }
            .filter { it.isIgnoredFile(projectDir, filesToExclude).not() } // Specific files to exclude
            .toList()

        filesToUpdate.forEach { file ->
            if (!file.exists()) {
                println("Warning: File not found: ${file.absolutePath}")
                return@forEach
            }

            try {
                val updatedContent = file
                    .readText(StandardCharsets.UTF_8)
                    .replace(ACTUAL_PROJECT_CONFIGURATION_OBJECT, projectName) // Replace "MyProject"
                    .replace(ACTUAL_PACKAGE_NAME_CONFIGURATION_OBJECT, packageName)  // Replace "com.tweener.changehere"
                    .replace(ACTUAL_PROJECT_NAME, projectName) // Replace root project name
                    .replace(ACTUAL_MODULE_NAME, moduleName) // Replace module name
                    // Replace package and import statements
                    .replace("package $ACTUAL_PACKAGE_NAME_CONFIGURATION_OBJECT", "package $packageName")
                    .replace("import $ACTUAL_PACKAGE_NAME_CONFIGURATION_OBJECT", "import $packageName")

                if (dryRun) {
                    println("\n------ Dry run: File changes for ${file.absolutePath}:")
                    println(updatedContent)
                    updatedFiles.add("${file.absolutePath} (dry run)")
                } else {
                    file.writeText(updatedContent, StandardCharsets.UTF_8)
                    println("\nUpdated file: ${file.absolutePath}")
                    updatedFiles.add(file.absolutePath)
                }
            } catch (e: Exception) {
                println("Error updating file: ${file.absolutePath}. Reason: ${e.message}")
            }
        }
    }

    private fun File.isInsideBuildDirectory(): Boolean {
        var current: File? = this
        while (current != null) {
            if (current.name == BUILD_DIR) return true
            current = current.parentFile
        }
        return false
    }

    private fun File.isIgnoredFile(projectDir: File, excludedFiles: List<String>): Boolean =
        excludedFiles.any { excludedFile ->
            this.name == excludedFile && this.toPath().startsWith(File(projectDir, BUILD_SRC_DIR).toPath())
        }
}
