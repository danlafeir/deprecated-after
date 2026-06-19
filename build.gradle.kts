plugins {
    base
}

allprojects {
    group = "com.lafeir"
    version = "0.1.0"
}

val stagingRepo = layout.buildDirectory.dir("staging-deploy")

subprojects {
    plugins.withId("maven-publish") {
        plugins.withId("java") {
            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
                withJavadocJar()
            }
        }

        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "staging"
                    url = stagingRepo.get().asFile.toURI()
                }
            }
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(provider { "${rootProject.name}: ${project.name}" })
                    description.set(provider { project.description ?: project.name })
                    inceptionYear.set("2026")
                    url.set("https://github.com/danlafeir/deprecated-after")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("danlafeir")
                            name.set("Dan Lafeir")
                            url.set("https://github.com/danlafeir")
                        }
                    }
                    scm {
                        url.set("https://github.com/danlafeir/deprecated-after")
                        connection.set("scm:git:git://github.com/danlafeir/deprecated-after.git")
                        developerConnection.set("scm:git:ssh://git@github.com/danlafeir/deprecated-after.git")
                    }
                }
            }
        }

        plugins.withId("signing") {
            val signingKey = providers.gradleProperty("signingInMemoryKey")
                .orElse(providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey"))
            val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword")
                .orElse(providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"))

            val signing = extensions.getByType<SigningExtension>()
            signing.isRequired = signingKey.isPresent
            // Only sign when a key is configured, so publishToMavenLocal works keyless for testing.
            if (signingKey.isPresent) {
                signing.useInMemoryPgpKeys(signingKey.get(), signingPassword.orNull ?: "")
                extensions.getByType<PublishingExtension>().publications.all {
                    signing.sign(this)
                }
            }
        }
    }
}

// Assembles the signed artifacts into a single zip in the Central Portal bundle layout.
// Run after `publishAllPublicationsToStagingRepository` on every module. The upload itself
// (POST to the Central Portal) is a manual step documented in the README.
tasks.register<Zip>("centralBundle") {
    group = "publishing"
    description = "Zips build/staging-deploy into a Central Portal upload bundle."
    from(stagingRepo)
    destinationDirectory.set(layout.buildDirectory.dir("central"))
    archiveFileName.set("central-bundle-${project.version}.zip")
}
