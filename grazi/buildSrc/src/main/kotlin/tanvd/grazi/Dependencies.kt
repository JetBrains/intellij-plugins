package tanvd.grazi

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude

fun ExternalModuleDependency.kotlinExcludes() {
    // already in IDEA
    exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
    exclude("org.jetbrains", "annotations")
    exclude("org.slf4j", "slf4j-api")
}

fun ExternalModuleDependency.ltExcludes() {
    kotlinExcludes()

    // already in project
    exclude("org.apache.commons", "commons-lang3")

    // already in IDEA
    exclude("com.google.guava", "guava")

    exclude("net.java.dev.jna", "jna")

    exclude("javax.xml.bind", "jaxb-api")

    exclude("org.glassfish.jaxb", "jaxb-runtime")

    exclude("com.fasterxml.jackson.core", "jackson-databind")

    exclude("commons-logging", "commons-logging")
}
