package tanvd.grazi

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude


fun ExternalModuleDependency.ltExcludes() {
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
    exclude("com.esotericsoftware.kryo", "kryo")

    exclude("org.apache.commons", "commons-lang3")
    exclude("org.apache.commons", "commons-csv")
    exclude("commons-logging", "commons-logging")
    exclude("javax.activation", "javax.activation-api")
    exclude("org.ow2.asm", "asm")

    // used only in tests in languagetool-core
    exclude("org.apache.lucene", "lucene-core")
    exclude("org.apache.lucene", "lucene-backward-codecs")

    // exclude opennlp from english
    exclude("edu.washington.cs.knowitall", "opennlp-postag-models")
    exclude("edu.washington.cs.knowitall", "opennlp-chunk-models")
    exclude("edu.washington.cs.knowitall", "opennlp-tokenize-models")
    exclude("org.apache.opennlp", "opennlp-tools")
}

fun DependencyHandler.aetherDependencies() {
    _compile("org.eclipse.aether", "aether-connector-basic", "1.1.0")
    _compile("org.eclipse.aether", "aether-transport-file", "1.1.0")
    _compile("org.eclipse.aether", "aether-transport-http", "1.1.0") {
        exclude("org.slf4j", "slf4j-api")
        exclude("org.apache.httpcomponents", "httpclient")
    }
    _compile("org.apache.maven", "maven-aether-provider", "3.3.9") {
        exclude("org.slf4j", "slf4j-api")
        exclude("com.google.guava", "guava")
        exclude("org.apache.commons", "commons-lang3")
    }
}
