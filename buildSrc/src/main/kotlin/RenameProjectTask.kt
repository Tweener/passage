import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * @author Vivien Mahe
 * @since 27/11/2024
 */

abstract class RenameProjectTask : DefaultTask() {

    companion object {
        private const val PROJECT_NAME_TASK_PARAM = "projectName"
        private const val PACKAGE_NAME_TASK_PARAM = "packageName"
        private const val DRY_RUN_TASK_PARAM = "dryRun"

        private const val ACTUAL_PROJECT_NAME = "Kmplate_Library"
        private const val ACTUAL_MODULE_NAME = "changehere"
        private const val ACTUAL_PROJECT_CONFIGURATION_OBJECT = "MyProject" // For ProjectConfiguration.kt
        private const val ACTUAL_PACKAGE_NAME_CONFIGURATION_OBJECT = "com.tweener.changehere" // For ProjectConfiguration.kt

        private const val BUILD_DIR = "build"

        private const val GITHUB_WORKFLOW_BUILD_FILENAME = "buildRelease.yml"
        private const val GITHUB_WORKFLOW_NOTIFY_FILENAME = "notify.yml"
        private const val LIBRARY_BUILD_GRADLE_FILENAME = "build.gradle.kts"
        private const val SETTINGS_FILENAME = "settings.gradle.kts"
        private const val PROJECT_CONFIG_FILENAME = "ProjectConfiguration.kt"
        private const val THIS_TASK_FILENAME = "RenameProjectTask.kt"
    }

    init {
        group = "custom"
        description =
            "Renames the project's directories and updates references in files. Usage: ./gradlew renameProject -$PROJECT_NAME_TASK_PARAM=MyLibrary -$PACKAGE_NAME_TASK_PARAM=org.example.mylibrary"
    }

    @TaskAction
    fun renameProject() {
        // Get projectName property and validate it
        val projectName = project.findProperty(PROJECT_NAME_TASK_PARAM)?.toString()
            ?: throw IllegalArgumentException("You must pass the '$PROJECT_NAME_TASK_PARAM' property. Example: ./gradlew renameProject -P$PROJECT_NAME_TASK_PARAM=MyLibrary")

        require(projectName.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            "Invalid project name: $projectName. Only alphanumeric characters and underscores are allowed."
        }

        val moduleName = projectName.lowercase()

        // Get packageName property and validate it
        val packageName = project.findProperty(PACKAGE_NAME_TASK_PARAM)?.toString()
            ?: throw IllegalArgumentException("You must pass the '$PACKAGE_NAME_TASK_PARAM' property. Example: ./gradlew renameProject -P$PACKAGE_NAME_TASK_PARAM=org.example.mylibrary")

        require(packageName.matches(Regex("^[a-zA-Z][a-zA-Z0-9_.]+$"))) {
            "Invalid package name: $packageName. It must be a valid Java package name."
        }

        // Get dryRun property
        val dryRun = project.hasProperty(DRY_RUN_TASK_PARAM)

        println("Starting project rename task...")
        println("Target project name: $projectName")
        println("Derived module name: $moduleName")
        println("Target package name: $packageName")
        println(if (dryRun) "Dry run enabled. No changes will be applied." else "Applying changes...")

        // Step 1: Delete existing moduleName directory if it exists
        deleteExistingModuleDirectory(moduleName, dryRun)

        // Step 2: Rename directories
        val renamedDirectories = mutableListOf<String>()
        renameDirectories(moduleName = moduleName, dryRun = dryRun, renamedDirectories = renamedDirectories)

        // Step 3: Update files
        val updatedFiles = mutableListOf<String>()
        replaceWordsInFiles(projectName = projectName, moduleName = moduleName, packageName = packageName, dryRun = dryRun, updatedFiles = updatedFiles)

        // Step 4: Print summary
        printSummary(renamedDirectories = renamedDirectories, updatedFiles = updatedFiles, dryRun = dryRun)
    }

    private fun deleteExistingModuleDirectory(moduleName: String, dryRun: Boolean) {
        println("\n--- Deleting existing module directory ---")

        val moduleDir = File(project.projectDir, moduleName)

        if (moduleDir.exists()) {
            if (dryRun) {
                println(
                    "\n------ Dry run: Directory to be deleted: ${moduleDir.absolutePath}"
                )
            } else {
                try {
                    moduleDir.deleteRecursively()
                    println("Deleted existing directory: ${moduleDir.absolutePath}")
                } catch (e: Exception) {
                    println("Error deleting directory: ${moduleDir.absolutePath}. Reason: ${e.message}")
                }
            }
        } else {
            println("No existing directory to delete: ${moduleDir.absolutePath}")
        }
    }

    private fun replaceWordsInFiles(projectName: String, moduleName: String, packageName: String, dryRun: Boolean, updatedFiles: MutableList<String>) {
        println("\n--- Updating words in files ---")

        // Find all .kt files and specified configuration files
        val specificFilesToUpdate = listOf(GITHUB_WORKFLOW_BUILD_FILENAME, GITHUB_WORKFLOW_NOTIFY_FILENAME, SETTINGS_FILENAME, PROJECT_CONFIG_FILENAME)

        val filesToUpdate = project.projectDir
            .walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.name in specificFilesToUpdate || it.path.endsWith(LIBRARY_BUILD_GRADLE_FILENAME)) }
            .filter { it.isProjectTaskFile().not() } // Exclude this file
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

    private fun renameDirectories(moduleName: String, dryRun: Boolean, renamedDirectories: MutableList<String>) {
        println("\n--- Renaming directories ---")

        fun renameDirectory(directory: File) {
            // Skip "build" directories and their subdirectories
            if (directory.isInsideBuildDirectory()) {
                println("Skipping directory inside build: ${directory.absolutePath}")
                return
            }

            // Rename the current directory if it matches the target name
            if (directory.name == ACTUAL_MODULE_NAME) {
                val newDir = File(directory.parentFile, moduleName)
                if (dryRun) {
                    println("\n------ Dry run: Renamed would be: ${directory.absolutePath} -> ${newDir.absolutePath}")
                    renamedDirectories.add("${directory.absolutePath} -> ${newDir.absolutePath} (dry run)")
                } else {
                    if (directory.renameTo(newDir)) {
                        println("Renamed: ${directory.absolutePath} -> ${newDir.absolutePath}")
                        renamedDirectories.add("${directory.absolutePath} -> ${newDir.absolutePath}")
                    } else {
                        println("Failed to rename: ${directory.absolutePath}. Check if it's in use or locked.")
                    }
                }
            }

            // Process subdirectories after renaming the current directory
            directory.listFiles { file -> file.isDirectory }?.forEach { subDir ->
                renameDirectory(subDir)
            }
        }

        // Start recursion from the project root directory
        renameDirectory(project.projectDir)
    }

    private fun printSummary(renamedDirectories: List<String>, updatedFiles: List<String>, dryRun: Boolean) {
        println("\n--- Summary (${if (dryRun) "Dry Run" else "Actual Run"}) ---")

        println("Renamed directories:")
        if (renamedDirectories.isEmpty()) {
            println("None")
        } else {
            renamedDirectories.forEach { println(it) }
        }

        println("\nUpdated files:")
        if (updatedFiles.isEmpty()) {
            println("None")
        } else {
            updatedFiles.forEach {
                if (it.contains("(error:")) {
                    println("Error: $it")
                } else {
                    println(it)
                }
            }
        }

        println("----------------")
        println(if (dryRun) "Dry run completed successfully!" else "Task completed successfully!")
    }

    private fun File.isInsideBuildDirectory(): Boolean {
        var current: File? = this
        while (current != null) {
            if (current.name == BUILD_DIR) return true
            current = current.parentFile
        }
        return false
    }

    private fun File.isProjectTaskFile(): Boolean =
        this.name == THIS_TASK_FILENAME && this.toPath().startsWith(File(project.projectDir, "buildSrc").toPath())
}
