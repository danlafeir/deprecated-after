rootProject.name = "deprecated-after"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("deprecated-after-annotation")
include("deprecated-after-core")
include("deprecated-after-gradle-plugin")
include("deprecated-after-maven-plugin")
