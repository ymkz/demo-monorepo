plugins {
    id("common-conventions")
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    annotationProcessor(platform(libs.spring.boot.bom))
    implementation(platform(libs.junit.bom))

    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)

    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.test)
}
