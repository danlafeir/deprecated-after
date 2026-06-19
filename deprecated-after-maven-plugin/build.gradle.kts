plugins {
    `java-library`
    alias(libs.plugins.maven.plugin.development)
}

description = "Maven plugin that fails the build when @DeprecatedAfter code outlives the project version."

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

dependencies {
    implementation(project(":deprecated-after-core"))
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.plugin.annotations)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.maven.plugin.api)
    testImplementation(project(":deprecated-after-annotation"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

mavenPlugin {
    goalPrefix.set("deprecated-after")
}

// maven-plugin-tools generates the descriptor/help-mojo via Velocity, which is not
// configuration-cache compatible. Degrade gracefully for just these tasks.
tasks.matching {
    it.name == "generateMavenPluginDescriptor" || it.name == "generateMavenPluginHelpMojoSources"
}.configureEach {
    notCompatibleWithConfigurationCache("maven-plugin-tools (Velocity/Plexus) is not CC-compatible")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
