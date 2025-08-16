package org.danlafeir

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Kotlin unit test for the 'org.danlafeir.deprecated-after' plugin.
 */
class DeprecatedAfterKotlinTest {
    
    @Test
    fun `plugin registers validateDeprecatedAfter task`() {
        // Create a test project and apply the plugin
        val project: Project = ProjectBuilder.builder().build()
        project.plugins.apply("org.danlafeir.deprecated-after")

        // Verify the result
        assertNotNull(project.tasks.findByName("validateDeprecatedAfter"))
    }
    
    @Test
    fun `plugin applies without errors`() {
        // Create a test project
        val project: Project = ProjectBuilder.builder().build()
        
        // Apply the plugin - should not throw any exceptions
        assertDoesNotThrow {
            project.plugins.apply("org.danlafeir.deprecated-after")
        }
        
        // Verify plugin is applied
        assertTrue(project.plugins.hasPlugin("org.danlafeir.deprecated-after"))
    }
    
    @Test
    fun `validateDeprecatedAfter task has correct properties`() {
        // Create a test project and apply the plugin
        val project: Project = ProjectBuilder.builder().build()
        project.plugins.apply("org.danlafeir.deprecated-after")

        // Get the task
        val task = project.tasks.findByName("validateDeprecatedAfter")
        assertNotNull(task)
        
        // Verify task properties
        assertEquals("verification", task!!.group)
        assertEquals("Validates @DeprecatedAfter annotations against project version", task.description)
    }
}