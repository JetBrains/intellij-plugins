plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "org.intellij.plugin.mdx"

val realVersion = "1.0.221"
version = realVersion

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    pluginName.set("MDX")
    version.set("LATEST-EAP-SNAPSHOT")
    type.set("IU")
    plugins.set(listOf("JavaScriptLanguage", "org.intellij.plugins.markdown", "CSS"))
    downloadSources.set(true)
    updateSinceUntilBuild.set(true)
}

tasks {

    patchPluginXml {
        sinceBuild.set("221.5080")
        untilBuild.set("221.*")
        version.set(realVersion)
    }

    wrapper {
        gradleVersion = "7.2"
    }

}
