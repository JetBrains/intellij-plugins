package tanvd.grazi

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude

fun Project.gitBranch(): String {
    val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD").directory(rootProject.projectDir).start()
    val branch = process.inputStream.bufferedReader().readText()
    return branch.split("\n").single { it.isNotBlank() }
}

val Project.channel: String
    get() {
        val branch = gitBranch()
        return when {
            branch.startsWith("stable") -> {
                "stable"
            }
            branch.startsWith("dev") -> {
                "dev"
            }
            branch.startsWith("master") -> {
                "nightly"
            }
            else -> {
                "feature"
            }
        }
    }

inline fun <reified Value> jbProperties() = System.getProperties()
        .filterKeys { it.toString().startsWith("idea") || it.toString().startsWith("jb") &&
                ((it as String) != "idea.home.path" && it != "jb.vmOptionsFile")
        }.mapKeys { it.key as String }.mapValues { it.value as Value }.toMutableMap()

fun execArguments() = System.getProperty("exec.args", "").split(",")

fun ExternalModuleDependency.excludesForLT() {
    exclude("org.slf4j", "slf4j-api")

    // useless for languagetool-core
    exclude("com.typesafe.akka")
    exclude("org.scala-lang")
    exclude("biz.k11i", "xgboost-predictor")

    // already in IDEA
    exclude("com.google.guava", "guava")
    exclude("com.intellij", "annotations")
    exclude("net.java.dev.jna", "jna")
    exclude("javax.xml.bind", "jaxb-api")
    exclude("org.glassfish.jaxb", "jaxb-runtime")
    exclude("com.fasterxml.jackson.core", "jackson-databind")

    // used only in tests in languagetool-core
    exclude("org.apache.lucene", "lucene-core")
    exclude("org.apache.lucene", "lucene-backward-codecs")

    // exclude opennlp from english
    exclude("edu.washington.cs.knowitall", "opennlp-postag-models")
    exclude("edu.washington.cs.knowitall", "opennlp-chunk-models")
    exclude("edu.washington.cs.knowitall", "opennlp-tokenize-models")
    exclude("org.apache.opennlp", "opennlp-tools")
}

fun DependencyHandler.addAetherDependencies() {
    _compile ("org.eclipse.aether", "aether-connector-basic", "1.1.0")
    _compile ("org.eclipse.aether", "aether-transport-file", "1.1.0")
    _compile ("org.eclipse.aether", "aether-transport-http", "1.1.0") {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
    }
    _compile("org.apache.maven", "maven-aether-provider", "3.3.9"){
        exclude("org.slf4j", "slf4j-api")
        exclude("com.google.guava", "guava")
        exclude("org.apache.commons", "commons-lang3")
    }
}
