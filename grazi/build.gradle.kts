import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import tanvd.grazi.Versions
import tanvd.grazi.channel

group = "tanvd.grazi"
version = "2019.2-3.5.$channel"


plugins {
    id("tanvd.kosogor") version "1.0.7" apply true
    id("io.gitlab.arturbosch.detekt") version ("1.0.0-RC14") apply true
    id("org.jetbrains.intellij") version "0.4.9" apply true
    kotlin("jvm") version "1.3.41" apply true
}

repositories {
    mavenCentral()
    jcenter()
}

intellij {
    pluginName = "Grazi"
    version = "IU-192.5728-EAP-CANDIDATE-SNAPSHOT"
    downloadSources = true
    type = "IU"

    updateSinceUntilBuild = false

    setPlugins(
            "markdown",
            "Kotlin",
            "java",
            "PythonCore:2019.2.192.5728.74",
            "org.rust.lang:0.2.101.2129-192",
            "nl.rubensten.texifyidea:0.6.6",
            "CSS",
            "JavaScriptLanguage",
            "properties"
    )
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.3"
        apiVersion = "1.3"
    }
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx1g")
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

    compile("org.languagetool", "languagetool-core", Versions.languageTool) {
        exclude("org.slf4j", "slf4j-api")
    }
    for (lang in langs) {
        compile("org.languagetool", "language-$lang", Versions.languageTool) {
            exclude("org.slf4j", "slf4j-api")
        }
    }

    compile("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.6.11")

    compile("org.apache.commons", "commons-lang3", "3.5")

    compile("tanvd.kex", "kex", "0.1.1")
}

tasks.withType<Test> {
    useJUnit()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
