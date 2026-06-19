plugins {
    `java-library`
}

description = "Core scanner and version logic shared by the deprecated-after build-tool plugins."

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

dependencies {
    implementation(libs.asm)

    testImplementation(libs.junit.jupiter)
    testImplementation(project(":deprecated-after-annotation"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
