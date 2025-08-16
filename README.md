# DeprecatedAfter Gradle Plugin

A Gradle plugin that validates `@DeprecatedAfter` annotations against the current project version and breaks the build when deprecated elements should be removed.

## Features

- **Version-based deprecation**: Mark elements for removal after specific versions
- **Automatic validation**: Runs during compilation to check deprecated elements
- **Build failure**: Breaks the build when deprecated code should be removed
- **Semantic versioning support**: Properly compares version numbers
- **Java and Kotlin support**: Annotations available for both languages

## How to Run the Tests

### Run All Tests
```bash
./gradlew test
```

## How to Build It

### Build the Plugin
```bash
./gradlew build
```

The built plugin JAR will be available in `plugin/build/libs/`.

## How to Use It

### 1. Apply the Plugin

Add to your `build.gradle.kts`:
```kotlin
plugins {
    id("org.danlafeir.deprecated-after") version "1.0.0"
}

version = "1.0.0" // Required for validation
```

Or in `build.gradle`:
```groovy
plugins {
    id 'org.danlafeir.deprecated-after' version '1.0.0'
}

version = '1.0.0' // Required for validation
```

### 2. Use the Annotation

#### Java Example
```java
import org.danlafeir.DeprecatedAfter;

@DeprecatedAfter(
    value = "2.0.0", 
    reason = "Legacy API", 
    replacement = "NewApiClass"
)
public class OldApiClass {
    // This will cause build failure when project version >= 2.0.0
}

@DeprecatedAfter("1.5.0")
public void oldMethod() {
    // This method should be removed after version 1.5.0
}
```

#### Kotlin Example
```kotlin
import org.danlafeir.DeprecatedAfter

@DeprecatedAfter(
    value = "2.0.0", 
    reason = "Use new coroutine-based API", 
    replacement = "newSuspendFunction"
)
fun oldFunction() {
    // This function should be removed after version 2.0.0
}
```

### 3. Validation

The plugin automatically validates annotations during compilation. You can also run manual validation:

```bash
./gradlew validateDeprecatedAfter
```

### 4. Example Build Failure

When your project version reaches or exceeds the deprecation version:

```
> Task :validateDeprecatedAfter FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':validateDeprecatedAfter'.
> @DeprecatedAfter validation failed! The following deprecated elements should be removed:
  - com.example.OldClass (deprecated after version 1.0.0) - Reason: Legacy API - Use: NewClass
  - com.example.MyClass.oldMethod() (deprecated after version 1.0.0)
  Current project version: 1.0.0
```

## How to Publish to Gradle Plugin Portal

### 1. Set Up Credentials

Create `~/.gradle/gradle.properties` with your Gradle Plugin Portal credentials:
```properties
gradle.publish.key=your-api-key
gradle.publish.secret=your-api-secret
```

### 2. Configure Publishing

Add to `plugin/build.gradle.kts`:
```kotlin
gradlePlugin {
    website = "https://github.com/danlafeir/deprecated-after"
    vcsUrl = "https://github.com/danlafeir/deprecated-after.git"
    
    val deprecatedAfter by plugins.creating {
        id = "org.danlafeir.deprecated-after"
        implementationClass = "org.danlafeir.DeprecatedAfter"
        displayName = "DeprecatedAfter Plugin"
        description = "Validates @DeprecatedAfter annotations against project version"
        tags = listOf("deprecated", "validation", "version", "cleanup")
    }
}
```

### 3. Publish

```bash
# Validate the plugin
./gradlew validatePlugins

# Publish to Gradle Plugin Portal
./gradlew publishPlugins
```

### 4. Local Publishing (for testing)

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal

# Use in other projects
repositories {
    mavenLocal()
    gradlePluginPortal()
}
```

## Configuration

### Project Version Requirement

The plugin requires your project to have a version set. If no version is specified, validation will be skipped with a warning.

```kotlin
version = "1.0.0" // Required
```

### Supported Version Formats

The plugin supports semantic versioning:
- `1.0.0`
- `1.0.0-SNAPSHOT`
- `1.0.0-alpha`
- `2.1.5`

## Annotation Parameters

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `value` | Yes | Version after which element should be removed | `"2.0.0"` |
| `reason` | No | Reason for deprecation | `"Legacy API"` |
| `replacement` | No | Suggested replacement | `"NewClass"` |

## Supported Elements

The `@DeprecatedAfter` annotation can be applied to:
- Classes and interfaces
- Methods and functions
- Fields and properties
- Constructors

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.