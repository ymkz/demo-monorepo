plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.springdoc.openapi)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    annotationProcessor(platform(libs.spring.boot.bom))
    implementation(platform(libs.jackson.bom))
    testImplementation(platform(libs.junit.bom))

    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)

    implementation(libs.bundles.api)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.test)

    implementation(project(":backend:core"))
}

openApi {
    apiDocsUrl.set("http://localhost:8080/spec/openapi.json")
    outputDir.set(rootProject.file("document/apispec"))
    outputFileName.set("openapi.json")
}

tasks.named("build") {
    dependsOn("generateOpenApiDocs")
}
