package com.lafeir.deprecatedafter.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckDeprecatedAfterMojoTest {

    private static final String SOURCE =
            "import com.lafeir.deprecatedafter.DeprecatedAfter;\n" +
            "@DeprecatedAfter(\"1.0.0\")\n" +
            "public class Sample {}\n";

    @TempDir
    Path tmp;

    private Path classesDir;

    @BeforeEach
    void compileFixture() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path src = tmp.resolve("Sample.java");
        Files.writeString(src, SOURCE);
        classesDir = tmp.resolve("classes");
        Files.createDirectories(classesDir);
        List<String> options = List.of(
                "-classpath", System.getProperty("java.class.path"),
                "-d", classesDir.toString());
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> units =
                    fm.getJavaFileObjectsFromFiles(List.of(src.toFile()));
            assertTrue(compiler.getTask(null, fm, null, options, null, units).call(),
                    "fixture compilation failed");
        }
    }

    private CheckDeprecatedAfterMojo mojo(String version, boolean skip) {
        CheckDeprecatedAfterMojo mojo = new CheckDeprecatedAfterMojo();
        setField(mojo, "outputDirectory", classesDir.toFile());
        setField(mojo, "projectVersion", version);
        setField(mojo, "skip", skip);
        return mojo;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void passesWhenVersionAtThreshold() {
        assertDoesNotThrow(() -> mojo("1.0.0", false).execute());
    }

    @Test
    void failsWhenVersionPastThreshold() {
        MojoFailureException ex =
                assertThrows(MojoFailureException.class, () -> mojo("1.0.1", false).execute());
        assertTrue(ex.getMessage().contains("Sample"), ex.getMessage());
    }

    @Test
    void skipBypassesValidation() {
        assertDoesNotThrow(() -> mojo("9.9.9", true).execute());
    }
}
