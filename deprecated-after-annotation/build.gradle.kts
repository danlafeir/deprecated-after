plugins {
    `java-library`
}

description = "The @DeprecatedAfter annotation: mark code for version-gated removal."

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}
