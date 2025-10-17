plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    annotationProcessor(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.boot.bom))
    testImplementation(platform(libs.junit.bom))

    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)

    implementation(libs.spring.boot.starter.validation)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.unit.test)
}
