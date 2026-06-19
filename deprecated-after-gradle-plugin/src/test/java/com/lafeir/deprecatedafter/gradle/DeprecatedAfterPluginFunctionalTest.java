package com.lafeir.deprecatedafter.gradle;

import com.lafeir.deprecatedafter.DeprecatedAfter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeprecatedAfterPluginFunctionalTest {

    @TempDir
    Path projectDir;

    /** Locate the compiled annotation on this test's classpath so the consumer build can compile against it. */
    private static String annotationClasspath() {
        try {
            File location = new File(
                    DeprecatedAfter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return location.getAbsolutePath().replace("\\", "\\\\");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeConsumerProject(String version) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'consumer'\n");
        Files.writeString(projectDir.resolve("build.gradle"),
                "plugins { id 'java'; id 'com.lafeir.deprecated-after' }\n"
                        + "version = '" + version + "'\n"
                        + "dependencies { compileOnly files('" + annotationClasspath() + "') }\n");

        Path src = projectDir.resolve("src/main/java/com/example");
        Files.createDirectories(src);
        Files.writeString(src.resolve("Old.java"),
                "package com.example;\n"
                        + "import com.lafeir.deprecatedafter.DeprecatedAfter;\n"
                        + "@DeprecatedAfter(value = \"1.0.0\", replacement = \"NewThing\")\n"
                        + "public class Old {}\n");
    }

    private GradleRunner runner() {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("validateDeprecatedAfter");
    }

    @Test
    void passesWhenVersionAtThreshold() throws IOException {
        writeConsumerProject("1.0.0");
        BuildResult result = runner().build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":validateDeprecatedAfter").getOutcome());
    }

    @Test
    void failsWhenVersionPastThreshold() throws IOException {
        writeConsumerProject("1.0.1");
        BuildResult result = runner().buildAndFail();
        assertTrue(result.getOutput().contains("should be removed"), result.getOutput());
        assertTrue(result.getOutput().contains("com.example.Old"), result.getOutput());
        assertTrue(result.getOutput().contains("Use: NewThing"), result.getOutput());
    }

    @Test
    void checkTaskTriggersValidation() throws IOException {
        writeConsumerProject("2.0.0");
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("check")
                .buildAndFail();
        assertEquals(TaskOutcome.FAILED, result.task(":validateDeprecatedAfter").getOutcome());
    }
}
