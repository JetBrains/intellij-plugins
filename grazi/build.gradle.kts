import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import tanvd.grazi.Versions
import tanvd.grazi.channel

group = "tanvd.grazi"
version = "2019.2-2.$channel"


plugins {
    id("tanvd.kosogor") version "1.0.5" apply true
    id("io.gitlab.arturbosch.detekt") version ("1.0.0-RC14") apply true
    id("org.jetbrains.intellij") version "0.4.5" apply true
    kotlin("jvm") version "1.3.31" apply true
}

repositories {
    mavenCentral()
    jcenter()
}

intellij {
    pluginName = "Grazi"
    version = "IC-192.4488-EAP-CANDIDATE-SNAPSHOT"
    downloadSources = true

    updateSinceUntilBuild = false

    setPlugins(
            "org.intellij.plugins.markdown:192.4488.21",
            "org.jetbrains.kotlin:1.3.31-release-IJ2019.1-1",
            "PythonCore:2019.2.192.4488.21"
    )
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.3"
        apiVersion = "1.3"
    }
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

val langs = setOf("en", "ru", "fr", "de", "pl", "it", "zh", "ja", "uk", "el", "ro", "es", "pt", "sk", "fa", "nl")

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))

    compile("org.languagetool", "languagetool-core", Versions.languageTool) {
        exclude("org.slf4j", "slf4j-api")
    }
    for (lang in langs) {
        compile("org.languagetool", "language-$lang", Versions.languageTool) {
            exclude("org.slf4j", "slf4j-api")
        }
    }

    compile("org.apache.commons", "commons-lang3", "3.5")

    compile("com.beust", "klaxon", "5.0.1")

    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.2.0")
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", "5.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
