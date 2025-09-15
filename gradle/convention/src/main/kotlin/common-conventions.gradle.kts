plugins {
    java
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "dev.ymkz.demo"
version = "1.0.0"

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        java {
            setSrcDirs(listOf("src/intTest/java"))
        }
        resources {
            setSrcDirs(listOf("src/intTest/resources"))
        }
    }
}

configurations {
    named("compileOnly") {
        extendsFrom(configurations.annotationProcessor.get())
    }
    named("intTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    named("intTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

spotless {
    java {
        palantirJavaFormat() // https://mvnrepository.com/artifact/com.palantir.javaformat/palantir-java-format
    }
    kotlinGradle {
        ktlint() // https://mvnrepository.com/artifact/com.pinterest.ktlint/ktlint-ruleset-standard
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("intTest") {
    useJUnitPlatform()
    group = "verification"
    description = "Runs the integration-test suite."
    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
}

tasks.named("build") {
    dependsOn("spotlessApply")
}

tasks.named("check") {
    dependsOn("spotlessCheck", "intTest")
}
