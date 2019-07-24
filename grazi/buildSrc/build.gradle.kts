repositories {
    jcenter()
    gradlePluginPortal()
}

plugins {
    id("tanvd.kosogor") version "1.0.7" apply true
    `kotlin-dsl` apply true
}


dependencies {
    compileOnly(gradleApi())
    api("gradle.plugin.org.jetbrains.intellij.plugins", "gradle-intellij-plugin", "0.4.9")
}

