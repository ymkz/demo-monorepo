rootProject.name = "demo"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention
}

includeBuild(
    "gradle/convention",
)

include(
    ":apps:core",
    ":apps:api",
)
