package com.lafeir.deprecatedafter.maven;

import com.lafeir.deprecatedafter.core.DeprecatedAfterScanner;
import com.lafeir.deprecatedafter.core.Violation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Scans the project's compiled classes for {@code @DeprecatedAfter} annotations and fails the
 * build when an element's recorded version has been surpassed by the project version. Bound to
 * the {@code verify} phase by default.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckDeprecatedAfterMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.version}", required = true)
    private String projectVersion;

    @Parameter(property = "deprecatedAfter.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoFailureException {
        if (skip) {
            getLog().info("Skipping @DeprecatedAfter validation (deprecatedAfter.skip=true).");
            return;
        }
        if (projectVersion == null || projectVersion.isEmpty() || "unspecified".equals(projectVersion)) {
            getLog().warn("Project version is unspecified; skipping @DeprecatedAfter validation.");
            return;
        }
        if (outputDirectory == null || !outputDirectory.isDirectory()) {
            getLog().info("No compiled classes at " + outputDirectory + "; nothing to validate.");
            return;
        }

        List<Violation> violations = new DeprecatedAfterScanner()
                .scan(Collections.singletonList(outputDirectory.toPath()), projectVersion);
        if (violations.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder(
                "@DeprecatedAfter validation failed! The following deprecated elements should be removed:\n");
        for (Violation violation : violations) {
            message.append("  - ").append(violation.describe()).append("\n");
        }
        message.append("Current project version: ").append(projectVersion);
        throw new MojoFailureException(message.toString());
    }
}
