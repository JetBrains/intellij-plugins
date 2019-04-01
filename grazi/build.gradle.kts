import org.jetbrains.intellij.tasks.PublishTask
import tanvd.grazi.Versions
import tanvd.grazi.channel

group = "tanvd.grazi"
version = "0.1.1.$channel"


plugins {
    id("tanvd.kosogor") version "1.0.4" apply true
    id("io.gitlab.arturbosch.detekt") version ("1.0.0-RC14") apply true
    id("org.jetbrains.intellij") version "0.4.5" apply true
    kotlin("jvm") version "1.3.21" apply true
}

repositories {
    mavenCentral()
    jcenter()
}

intellij {
    pluginName = "Grazi"
    version = "2019.1"
    downloadSources = true

    setPlugins(
            "org.intellij.plugins.markdown:191.5849.16",
            "org.jetbrains.kotlin:1.3.21-release-IJ2019.1-2"
    )
}

tasks.withType<PublishTask> {
    username(System.getenv("publish_username"))
    token(System.getenv("publish_token"))
    channels(channel)
}

detekt {
    parallel = true
    failFast = false
    config = files(File(project.rootProject.projectDir, "buildScripts/detekt/detekt.yml"))
    reports {
        xml {
            enabled = false
        }
        html {
            enabled = false
        }
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))

    compile("org.languagetool", "languagetool-core", Versions.languageTool) {
        exclude("org.slf4j", "slf4j-api")
    }
    compile("org.languagetool", "language-all", Versions.languageTool) {
        exclude("org.slf4j", "slf4j-api")
    }

    compile("org.apache.commons", "commons-lang3", "3.5")
    compile("com.github.ben-manes.caffeine", "caffeine", "2.7.0")

    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.2.0")
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", "5.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
