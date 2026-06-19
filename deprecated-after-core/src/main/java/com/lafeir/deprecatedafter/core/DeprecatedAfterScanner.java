package com.lafeir.deprecatedafter.core;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scans compiled {@code .class} files for {@code @DeprecatedAfter} annotations and reports the
 * ones whose recorded version has been surpassed by the current project version.
 *
 * <p>Reads annotations straight from bytecode with ASM (no classloading), so it works without
 * the consumer's full runtime classpath and regardless of the annotation's retention.
 */
public final class DeprecatedAfterScanner {

    private static final int ASM_API = Opcodes.ASM9;
    static final String ANNOTATION_DESCRIPTOR = "Lcom/lafeir/deprecatedafter/DeprecatedAfter;";

    /**
     * @param classDirectories directories containing compiled classes (non-existent ones are skipped)
     * @param projectVersion   the current project version
     * @return elements whose {@code @DeprecatedAfter} version is strictly less than {@code projectVersion}
     */
    public List<Violation> scan(Iterable<Path> classDirectories, String projectVersion) {
        SemanticVersion current = SemanticVersion.parse(projectVersion);

        List<Violation> annotated = new ArrayList<>();
        for (Path dir : classDirectories) {
            if (dir != null && Files.isDirectory(dir)) {
                collectFromDirectory(dir, annotated);
            }
        }

        List<Violation> violations = new ArrayList<>();
        for (Violation candidate : annotated) {
            if (current.compareTo(SemanticVersion.parse(candidate.getAfterVersion())) > 0) {
                violations.add(candidate);
            }
        }
        return violations;
    }

    private void collectFromDirectory(Path dir, List<Violation> out) {
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> collectFromClassFile(p, out));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan " + dir, e);
        }
    }

    private void collectFromClassFile(Path classFile, List<Violation> out) {
        try (InputStream in = Files.newInputStream(classFile)) {
            new ClassReader(in).accept(
                    new DeprecatedAfterClassVisitor(out),
                    ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + classFile, e);
        }
    }

    private static final class DeprecatedAfterClassVisitor extends ClassVisitor {

        private final List<Violation> out;
        private String className;

        DeprecatedAfterClassVisitor(List<Violation> out) {
            super(ASM_API);
            this.out = out;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (ANNOTATION_DESCRIPTOR.equals(descriptor)) {
                return new MemberCollector(className, out);
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            String element = name.equals("<init>")
                    ? className + ".<init>"
                    : className + "." + name + "()";
            return new MethodVisitor(ASM_API) {
                @Override
                public AnnotationVisitor visitAnnotation(String d, boolean visible) {
                    if (ANNOTATION_DESCRIPTOR.equals(d)) {
                        return new MemberCollector(element, out);
                    }
                    return null;
                }
            };
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                       String signature, Object value) {
            String element = className + "." + name;
            return new FieldVisitor(ASM_API) {
                @Override
                public AnnotationVisitor visitAnnotation(String d, boolean visible) {
                    if (ANNOTATION_DESCRIPTOR.equals(d)) {
                        return new MemberCollector(element, out);
                    }
                    return null;
                }
            };
        }
    }

    private static final class MemberCollector extends AnnotationVisitor {

        private final String elementName;
        private final List<Violation> out;
        private String value;
        private String reason = "";
        private String replacement = "";

        MemberCollector(String elementName, List<Violation> out) {
            super(ASM_API);
            this.elementName = elementName;
            this.out = out;
        }

        @Override
        public void visit(String name, Object val) {
            if (name == null) {
                return;
            }
            switch (name) {
                case "value":
                    value = String.valueOf(val);
                    break;
                case "reason":
                    reason = String.valueOf(val);
                    break;
                case "replacement":
                    replacement = String.valueOf(val);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void visitEnd() {
            if (value != null) {
                out.add(new Violation(elementName, value, reason, replacement));
            }
        }
    }
}
