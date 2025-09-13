rootProject.name = "demo"

includeBuild(
    "gradle/convention",
)

include(
    ":apps:core",
    ":apps:api",
)
