package com.lafeir.deprecatedafter.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeprecatedAfterPluginTest {

    @Test
    void registersValidateTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.lafeir.deprecated-after");

        var task = project.getTasks().findByName("validateDeprecatedAfter");
        assertNotNull(task);
        assertEquals("verification", task.getGroup());
    }
}
