plugins {
    `java-gradle-plugin`
}

description = "Gradle plugin that fails the build when @DeprecatedAfter code outlives the project version."

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

dependencies {
    implementation(project(":deprecated-after-core"))

    testImplementation(libs.junit.jupiter)
    testImplementation(gradleTestKit())
    testImplementation(project(":deprecated-after-annotation"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        create("deprecatedAfter") {
            id = "com.lafeir.deprecated-after"
            implementationClass = "com.lafeir.deprecatedafter.gradle.DeprecatedAfterPlugin"
            displayName = "DeprecatedAfter"
            description = "Fails the build when @DeprecatedAfter code outlives the project version."
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
