import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask
import tanvd.grazi.*

intellij {
    pluginName = "Grazi"
    version = "2019.2"
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

    alternativeIdePath = System.getProperty("idea.gui.test.alternativeIdePath")
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx2g")
}

tasks.withType<PublishTask> {
    username(System.getenv("publish_username"))
    token(System.getenv("publish_token"))
    channels(channel)
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
