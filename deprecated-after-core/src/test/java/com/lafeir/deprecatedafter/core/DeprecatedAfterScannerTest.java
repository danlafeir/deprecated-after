package com.lafeir.deprecatedafter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeprecatedAfterScannerTest {

    private static final String SOURCE =
            "import com.lafeir.deprecatedafter.DeprecatedAfter;\n" +
            "@DeprecatedAfter(value = \"1.0.0\", reason = \"legacy\", replacement = \"NewType\")\n" +
            "public class Sample {\n" +
            "    @DeprecatedAfter(\"2.0.0\") public void oldMethod() {}\n" +
            "    @DeprecatedAfter(\"3.0.0\") public String oldField = \"\";\n" +
            "}\n";

    private final DeprecatedAfterScanner scanner = new DeprecatedAfterScanner();

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
            boolean ok = compiler.getTask(null, fm, null, options, null, units).call();
            assertTrue(ok, "fixture compilation failed");
        }
    }

    @Test
    void noViolationsWhenVersionAtOrBelowThresholds() {
        assertEquals(0, scanner.scan(List.of(classesDir), "1.0.0").size());
    }

    @Test
    void classViolatesOncePastItsVersion() {
        List<Violation> violations = scanner.scan(List.of(classesDir), "1.0.1");
        assertEquals(1, violations.size());
        Violation v = violations.get(0);
        assertEquals("Sample", v.getElementName());
        assertEquals("1.0.0", v.getAfterVersion());
        assertEquals("legacy", v.getReason());
        assertEquals("NewType", v.getReplacement());
    }

    @Test
    void allElementsViolateWhenVersionPastAll() {
        assertEquals(3, scanner.scan(List.of(classesDir), "3.0.1").size());
    }

    @Test
    void snapshotOfThresholdDoesNotViolateThatElement() {
        // 2.0.0-SNAPSHOT > 1.0.0 (class) but not > 2.0.0 (method) and not > 3.0.0 (field)
        List<Violation> violations = scanner.scan(List.of(classesDir), "2.0.0-SNAPSHOT");
        assertEquals(1, violations.size());
        assertEquals("Sample", violations.get(0).getElementName());
    }

    @Test
    void missingDirectoryIsSkipped() {
        assertEquals(0, scanner.scan(List.of(tmp.resolve("does-not-exist")), "9.9.9").size());
    }
}
