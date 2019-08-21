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
    compile(kotlin("stdlib", "1.3.31"))

    compile("org.languagetool", "languagetool-core", Versions.languageTool) {
        ltExcludes()
    }

    compile("org.languagetool", "language-en", Versions.languageTool) {
        ltExcludes()
    }

    // for PyCharm and others no Intellij Idea applications
    aetherDependencies()
    compile("org.apache.commons", "commons-lang3", "3.5")

    compile("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.6.11") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains", "annotations")
    }

    compile("tanvd.kex", "kex", "0.1.1") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    for (lang in langs) {
        testRuntime("org.languagetool", "language-$lang", Versions.languageTool) {
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
