import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import tanvd.grazi.*

group = rootProject.group
version = rootProject.version

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

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx1g")
}

tasks.withType<PublishTask> {
    username(System.getenv("publish_username"))
    token(System.getenv("publish_token"))
    channels(channel)
}

val langs = setOf("ru", "fr", "de", "pl", "it", "zh", "ja", "uk", "el", "ro", "es", "pt", "sk", "fa", "nl")

dependencies {
    compileOnly(kotlin("stdlib"))
    testCompile(kotlin("stdlib"))

    compile("tanvd.grazi", "language-detector", "0.1.0") {
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

tasks.withType<Test> {
    useJUnit()
    // need for enabling all 16 languages for tests
    jvmArgs("-Xmx1g")

    testLogging {
        events("passed", "skipped", "failed")
    }
}
