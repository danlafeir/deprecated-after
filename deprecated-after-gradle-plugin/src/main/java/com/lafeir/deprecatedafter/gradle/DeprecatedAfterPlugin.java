package com.lafeir.deprecatedafter.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * Registers the {@code validateDeprecatedAfter} task and wires it into {@code check} so the
 * build fails when {@code @DeprecatedAfter} code outlives the project version.
 */
public class DeprecatedAfterPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        TaskProvider<ValidateDeprecatedAfterTask> validate = project.getTasks().register(
                "validateDeprecatedAfter", ValidateDeprecatedAfterTask.class, task -> {
                    task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                    task.setDescription("Validates @DeprecatedAfter annotations against the project version.");
                    task.getProjectVersion().set(project.provider(() -> String.valueOf(project.getVersion())));
                });

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            validate.configure(task -> task.getClassesDirs().from(main.getOutput().getClassesDirs()));
        });

        project.getPlugins().withType(LifecycleBasePlugin.class, lifecycle ->
                project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME)
                        .configure(check -> check.dependsOn(validate)));
    }
}
