plugins {
    `java-library`
    `maven-publish`
    signing
}

description = "The @DeprecatedAfter annotation: mark code for version-gated removal."

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
