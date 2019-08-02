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
            "markdown",
            "Kotlin",
            "PythonCore:2019.1.191.6183.53",
            "org.rust.lang:0.2.98.2125-191",
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

fun ExternalModuleDependency.excludesForLT() {
    exclude("org.slf4j", "slf4j-api")
    //exclude("ml.dmlc", "xgboost4j")
    exclude("com.typesafe.akka")
    exclude("org.scala-lang")
    exclude("biz.k11i", "xgboost-predictor")

    exclude("com.google.guava", "guava")
    exclude("com.intellij", "annotations")
    exclude("net.java.dev.jna", "jna")

    exclude("javax.xml.bind", "jaxb-api")
    exclude("org.glassfish.jaxb", "jaxb-runtime")

    exclude("com.fasterxml.jackson.core", "jackson-databind")

    exclude("org.apache.lucene", "lucene-core")
    exclude("org.apache.lucene", "lucene-backward-codecs")
}

dependencies {
    compileOnly(kotlin("stdlib"))

    compile("org.languagetool", "languagetool-core", Versions.languageTool) {
        excludesForLT()
    }

    compile("org.languagetool", "language-en", Versions.languageTool) {
        excludesForLT()
    }

    testRuntime("org.languagetool", "language-ru", Versions.languageTool) {
        excludesForLT()
    }

    // for PyCharm and others no Intellij Idea applications
    compile ("org.eclipse.aether", "aether-connector-basic", "1.1.0")
    compile ("org.eclipse.aether", "aether-transport-file", "1.1.0")
    compile ("org.eclipse.aether", "aether-transport-http", "1.1.0") {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
    }
    compile("org.apache.maven", "maven-aether-provider", "3.3.9"){
        exclude("org.slf4j", "slf4j-api")
        exclude("com.google.guava", "guava")
        exclude("org.apache.commons", "commons-lang3")
    }
    // ---

    compile("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.6.11")

    //compile("org.apache.commons", "commons-lang3", "3.5")

    compile("tanvd.kex", "kex", "0.1.1")
}

tasks.withType<Test> {
    useJUnit()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
