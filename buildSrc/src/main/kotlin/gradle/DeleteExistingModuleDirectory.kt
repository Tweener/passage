package gradle

import java.io.File

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class DeleteExistingModuleDirectory {

    fun execute(projectDir: File, moduleName: String, dryRun: Boolean) {
        println("\n--- Deleting existing module directory ---")

        val moduleDir = File(projectDir, moduleName)

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
}
