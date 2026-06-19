# DeprecatedAfter

Mark code with `@DeprecatedAfter("x.y.z")` and have your **build fail once the project's
version moves past that version** — so deprecated code can't silently outlive its removal
window. Works identically under **Gradle** and **Maven**.

```java
@DeprecatedAfter(value = "2.0.0", reason = "Legacy API", replacement = "NewApiClass")
public class OldApiClass { }
```

Once the project version becomes strictly greater than `2.0.0` (e.g. `2.0.1`), the build fails
until `OldApiClass` is removed.

## How it works

- The `@DeprecatedAfter` annotation has **`CLASS` retention** — it is written to bytecode but not
  loaded at runtime, so you depend on it with `compileOnly` / `provided` scope and ship nothing
  extra in your application.
- A build-tool plugin scans your compiled classes (via ASM, no classloading) and compares each
  annotated element's version against the project version using Semantic Versioning.
- An element fails the build only when `projectVersion` is **strictly greater** than its
  `@DeprecatedAfter` value. Pre-releases sort below their release (`2.0.0-SNAPSHOT` < `2.0.0`), so
  a snapshot of the threshold version does not trip the gate.

## Artifacts

All published to Maven Central under group `com.lafeir`:

| Artifact | Purpose |
|----------|---------|
| `deprecated-after-annotation` | The `@DeprecatedAfter` annotation (zero dependencies). Add to your project. |
| `deprecated-after-gradle-plugin` | Gradle plugin (id `com.lafeir.deprecated-after`). |
| `deprecated-after-maven-plugin` | Maven plugin (goal `deprecated-after:check`). |
| `deprecated-after-core` | Shared scanner + version logic. Pulled in transitively by the plugins. |

## Gradle

The plugin is published to Maven Central (not the Gradle Plugin Portal), so add `mavenCentral()`
to plugin resolution in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

Then apply it and depend on the annotation in `build.gradle.kts`:

```kotlin
plugins {
    java
    id("com.lafeir.deprecated-after") version "0.1.0"
}

version = "1.0.0" // the version checked against @DeprecatedAfter

dependencies {
    compileOnly("com.lafeir:deprecated-after-annotation:0.1.0")
}
```

The plugin registers a `validateDeprecatedAfter` task and wires it into `check`, so it runs as
part of `./gradlew check` (or `./gradlew build`). Run it directly with:

```bash
./gradlew validateDeprecatedAfter
```

## Maven

Declare the annotation as a `provided` dependency and bind the plugin to the build:

```xml
<dependencies>
  <dependency>
    <groupId>com.lafeir</groupId>
    <artifactId>deprecated-after-annotation</artifactId>
    <version>0.1.0</version>
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>com.lafeir</groupId>
      <artifactId>deprecated-after-maven-plugin</artifactId>
      <version>0.1.0</version>
      <executions>
        <execution>
          <goals><goal>check</goal></goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

The `check` goal binds to the `verify` phase by default, so it runs on `mvn verify`. Skip it with
`-DdeprecatedAfter.skip=true`.

## The annotation

```java
import com.lafeir.deprecatedafter.DeprecatedAfter;

@DeprecatedAfter("1.5.0")
public void oldMethod() { }
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `value` | Yes | Version after which the element must be removed, e.g. `"2.0.0"`. |
| `reason` | No | Reason for deprecation, shown in the failure message. |
| `replacement` | No | Suggested replacement, shown in the failure message. |

Applicable to classes/interfaces, methods/constructors, and fields.

### Example failure

```
@DeprecatedAfter validation failed! The following deprecated elements should be removed:
  - com.example.OldApiClass (deprecated after version 2.0.0) - Reason: Legacy API - Use: NewApiClass
Current project version: 2.0.1
```

## Version handling

Versions are compared per Semantic Versioning 2.0.0:

- Numeric core compared field by field; missing trailing fields are treated as zero (`1.0` == `1.0.0`).
- A pre-release sorts **below** its release: `2.0.0-SNAPSHOT` and `2.0.0-alpha` are both `< 2.0.0`.
- Build metadata (`+sha`) is ignored.
- If the project version is unset/`unspecified`, validation is skipped with a warning.

## Building from source

```bash
./gradlew build               # compile + run all tests
./gradlew publishToMavenLocal # install all artifacts to ~/.m2 (signing skipped without a key)
```

## Publishing to Maven Central

Publishing uses Gradle's built-in `maven-publish` + `signing` (no third-party publishing plugin)
and targets the [Central Portal](https://central.sonatype.com).

Prerequisites:

1. **Verify the `com.lafeir` namespace** in the Central Portal (DNS TXT record on `lafeir.com`).
2. A Central Portal **publishing token**, exposed as:
   - `ORG_GRADLE_PROJECT_mavenCentralUsername`
   - `ORG_GRADLE_PROJECT_mavenCentralPassword`
3. A **GPG signing key**, exposed as:
   - `ORG_GRADLE_PROJECT_signingInMemoryKey` (ASCII-armored private key)
   - `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`

Then build the upload bundle and post it to the Portal:

```bash
./gradlew clean publishAllPublicationsToStagingRepository centralBundle
curl --request POST \
  --header "Authorization: Bearer $CENTRAL_PORTAL_TOKEN" \
  --form bundle=@build/central/central-bundle-0.1.0.zip \
  https://central.sonatype.com/api/v1/publisher/upload
```

## License

Apache License 2.0 — see the [LICENSE](LICENSE) file.
