import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import tanvd.grazi.*
import tanvd.kosogor.proxy.publishJar

group = "tanvd.grazi"
version = "2019.2-5.1.$channel"

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.labs.intellij.net/intdev")
}

plugins {
    id("tanvd.kosogor") version "1.0.7" apply true
    id("io.gitlab.arturbosch.detekt") version ("1.0.0-RC14") apply true
    id("org.jetbrains.intellij") apply true
    kotlin("jvm") version "1.3.31" apply true
}

intellij {
    pluginName = "Grazi"
    version = Versions.intellij
    downloadSources = true
    type = "IU"

    updateSinceUntilBuild = false

    setPlugins(
            "java",
            "markdown",
            "Kotlin",
            "PythonCore:2019.2.192.5728.74",
            "org.rust.lang:0.2.101.2129-192",
            "nl.rubensten.texifyidea:0.6.6",
            "CSS",
            "JavaScriptLanguage",
            "properties"
    )
}

val langs = setOf("ru", "fr", "de", "pl", "it", "zh", "ja", "uk", "el", "ro", "es", "pt", "sk", "fa", "nl")

dependencies {
    compileOnly(kotlin("stdlib"))
    testCompile(kotlin("stdlib"))

    compile("tanvd.grazi", "language-detector", "0.1.1") {
        kotlinExcludes()
    }

    compile("tanvd.grazi.languagetool", "languagetool-core", Versions.languageTool) {
        ltExcludes()
    }

    compile("tanvd.grazi.languagetool", "en", Versions.languageTool) {
        ltExcludes()
    }

    compile("org.apache.commons", "commons-lang3", "3.9")
    compile("org.apache.commons", "commons-text", "1.7")

    compile("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.6.11") {
        kotlinExcludes()
    }

    compile("tanvd.kex", "kex", "0.1.1") {
        kotlinExcludes()
    }

    for (lang in langs) {
        testRuntime("tanvd.grazi.languagetool", lang, Versions.languageTool) {
            ltExcludes()
        }
    }
}

publishJar {
    artifactory {
        serverUrl = "https://repo.labs.intellij.net"
        repository = "intdev"
        username = "intdev-publish"
        secretKey = System.getenv("artifactory_api_key")
    }
}


tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.3"
        apiVersion = "1.3"
    }
}


tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx1g", "-Dfus.internal.test.mode=true")
}

tasks.withType<PublishTask> {
    username(System.getenv("publish_username"))
    token(System.getenv("publish_token"))
    channels(channel)
}


tasks.withType<Test> {
    useJUnit()
    // need for enabling all 16 languages for tests
    jvmArgs("-Xmx1g")

    testLogging {
        events("passed", "skipped", "failed")
    }
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

