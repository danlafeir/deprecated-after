package com.lafeir.deprecatedafter.gradle;

import com.lafeir.deprecatedafter.core.DeprecatedAfterScanner;
import com.lafeir.deprecatedafter.core.Violation;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans the project's compiled classes for {@code @DeprecatedAfter} annotations and fails the
 * build when an element's recorded version has been surpassed by the project version.
 *
 * <p>Configuration-cache friendly: inputs (class directories, project version) are captured at
 * configuration time and the task action never touches the {@code Project}.
 */
public abstract class ValidateDeprecatedAfterTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getClassesDirs();

    @Input
    public abstract Property<String> getProjectVersion();

    @TaskAction
    public void validate() {
        String version = getProjectVersion().get();
        if (version.isEmpty() || "unspecified".equals(version)) {
            getLogger().warn("Project version is unspecified; skipping @DeprecatedAfter validation.");
            return;
        }

        List<Path> dirs = new ArrayList<>();
        for (File dir : getClassesDirs().getFiles()) {
            dirs.add(dir.toPath());
        }

        List<Violation> violations = new DeprecatedAfterScanner().scan(dirs, version);
        if (violations.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder(
                "@DeprecatedAfter validation failed! The following deprecated elements should be removed:\n");
        for (Violation violation : violations) {
            message.append("  - ").append(violation.describe()).append("\n");
        }
        message.append("Current project version: ").append(version);
        throw new GradleException(message.toString());
    }
}
