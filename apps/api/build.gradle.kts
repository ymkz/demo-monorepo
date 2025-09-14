plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.springdoc.openapi)
}

dependencies {
    annotationProcessor(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.jackson.bom))
    testImplementation(platform(libs.junit.bom))
    intTestImplementation(platform(libs.testcontainers.bom))

    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)

    implementation(libs.bundles.api)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.unit.test)
    intTestImplementation(libs.bundles.integration.test)

    implementation(project(":apps:core"))
}

openApi {
    apiDocsUrl.set("http://localhost:8080/openapi/openapi.json")
    outputDir.set(rootProject.file("apps/api/src/main/resources/static/openapi"))
    outputFileName.set("openapi.json")
}

tasks.named("build") {
    dependsOn("generateOpenApiDocs")
}
