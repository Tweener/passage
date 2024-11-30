package gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author Vivien Mahe
 * @since 27/11/2024
 */

abstract class RenameProjectTask : DefaultTask() {

    companion object {
        private const val PROJECT_NAME_TASK_PARAM = "projectName"
        private const val PACKAGE_NAME_TASK_PARAM = "packageName"
        private const val DRY_RUN_TASK_PARAM = "dryRun"

        internal const val ACTUAL_MODULE_NAME = "changehere"
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
        DeleteExistingModuleDirectory().execute(projectDir = project.projectDir, moduleName = moduleName, dryRun = dryRun)

        // Step 2: Rename directories
        val renamedDirectories = mutableListOf<String>()
        RenameDirectories().execute(projectDir = project.projectDir, moduleName = moduleName, packageName = packageName, dryRun = dryRun, renamedDirectories = renamedDirectories)

        // Step 3: Update files
        val updatedFiles = mutableListOf<String>()
        ReplaceWordsInFiles().execute(projectDir = project.projectDir, projectName = projectName, moduleName = moduleName, packageName = packageName, dryRun = dryRun, updatedFiles = updatedFiles)

        // Step 4: Print summary
        printSummary(renamedDirectories = renamedDirectories, updatedFiles = updatedFiles, dryRun = dryRun)
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
}
