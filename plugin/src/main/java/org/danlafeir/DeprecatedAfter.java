package org.danlafeir;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin that validates @DeprecatedAfter annotations against project version.
 */
public class DeprecatedAfter implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        // Add validation task that runs after compilation
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.doLast(t -> validateDeprecatedAfterAnnotations(project));
        });
        
        // Register a task for manual validation
        project.getTasks().register("validateDeprecatedAfter", task -> {
            task.setGroup("verification");
            task.setDescription("Validates @DeprecatedAfter annotations against project version");
            task.doLast(s -> validateDeprecatedAfterAnnotations(project));
        });
    }
    
    private void validateDeprecatedAfterAnnotations(Project project) {
        String projectVersion = project.getVersion().toString();
        if ("unspecified".equals(projectVersion)) {
            project.getLogger().warn("Project version is unspecified, skipping @DeprecatedAfter validation");
            return;
        }
        
        try {
            // Get compiled classes directory
            File classesDir = new File(project.getBuildDir(), "classes/java/main");
            if (!classesDir.exists()) {
                return; // No compiled classes yet
            }
            
            List<String> violations = findDeprecatedAfterViolations(classesDir, projectVersion);
            
            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("@DeprecatedAfter validation failed! The following deprecated elements should be removed:\n");
                for (String violation : violations) {
                    errorMessage.append("  - ").append(violation).append("\n");
                }
                errorMessage.append("Current project version: ").append(projectVersion);
                
                throw new GradleException(errorMessage.toString());
            }
            
        } catch (Exception e) {
            if (e instanceof GradleException) {
                throw e;
            }
            project.getLogger().warn("Failed to validate @DeprecatedAfter annotations: " + e.getMessage());
        }
    }
    
    private List<String> findDeprecatedAfterViolations(File classesDir, String projectVersion) throws Exception {
        List<String> violations = new ArrayList<>();
        
        // Create a classloader for the compiled classes
        URL[] urls = {classesDir.toURI().toURL()};
        try (URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader())) {
            
            // Find all .class files recursively
            findClassFiles(classesDir, "", classLoader, projectVersion, violations);
        }
        
        return violations;
    }
    
    private void findClassFiles(File dir, String packageName, URLClassLoader classLoader, 
                               String projectVersion, List<String> violations) throws Exception {
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                findClassFiles(file, subPackage, classLoader, projectVersion, violations);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                if (className.startsWith(".")) {
                    className = className.substring(1);
                }
                
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    checkClassForDeprecatedAfter(clazz, projectVersion, violations);
                } catch (Exception e) {
                    // Skip classes that can't be loaded
                }
            }
        }
    }
    
    private void checkClassForDeprecatedAfter(Class<?> clazz, String projectVersion, List<String> violations) {
        // Check class-level annotation
        checkAnnotation(clazz.getAnnotations(), clazz.getName(), projectVersion, violations);
        
        // Check methods
        for (Method method : clazz.getDeclaredMethods()) {
            checkAnnotation(method.getAnnotations(), 
                          clazz.getName() + "." + method.getName() + "()", 
                          projectVersion, violations);
        }
        
        // Check fields
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            checkAnnotation(field.getAnnotations(), 
                          clazz.getName() + "." + field.getName(), 
                          projectVersion, violations);
        }
        
        // Check constructors
        for (java.lang.reflect.Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            checkAnnotation(constructor.getAnnotations(), 
                          clazz.getName() + ".<constructor>", 
                          projectVersion, violations);
        }
    }
    
    private void checkAnnotation(Annotation[] annotations, String elementName, 
                               String projectVersion, List<String> violations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getName().equals("org.danlafeir.DeprecatedAfter")) {
                try {
                    Method valueMethod = annotation.annotationType().getMethod("value");
                    String annotationVersion = (String) valueMethod.invoke(annotation);
                    
                    if (shouldBeRemoved(annotationVersion, projectVersion)) {
                        String reason = "";
                        String replacement = "";
                        
                        try {
                            Method reasonMethod = annotation.annotationType().getMethod("reason");
                            reason = (String) reasonMethod.invoke(annotation);
                            
                            Method replacementMethod = annotation.annotationType().getMethod("replacement");
                            replacement = (String) replacementMethod.invoke(annotation);
                        } catch (Exception e) {
                            // Ignore if methods don't exist
                        }
                        
                        String violation = elementName + " (deprecated after version " + annotationVersion + ")";
                        if (!reason.isEmpty()) {
                            violation += " - Reason: " + reason;
                        }
                        if (!replacement.isEmpty()) {
                            violation += " - Use: " + replacement;
                        }
                        
                        violations.add(violation);
                    }
                } catch (Exception e) {
                    // Skip if we can't read the annotation
                }
            }
        }
    }
    
    private boolean shouldBeRemoved(String deprecatedAfterVersion, String currentVersion) {
        // Simple version comparison - assumes semantic versioning
        return compareVersions(currentVersion, deprecatedAfterVersion) >= 0;
    }
    
    private int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");
        
        int maxLength = Math.max(v1Parts.length, v2Parts.length);
        
        for (int i = 0; i < maxLength; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            
            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }
        
        return 0; // Versions are equal
    }
}