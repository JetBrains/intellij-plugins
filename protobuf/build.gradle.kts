import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "../contrib-configuration/common.gradle.kts")

/**
 * Initialize this property in a specific Gradle task to determine the plugin runtime layout
 * that is different for various compatible IDEs
 */
val defaultPluginRunMode = ProtobufPluginLayout.ProtobufCoreTestsInIdeaCommunity("2025.1.2")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform")
}

repositories {
  intellijPlatform {
    defaultRepositories()
    snapshots()
  }
}

intellijPlatform {
  pluginConfiguration {
    name = "protobuf"
  }
}

dependencies {
  implementation("com.google.protobuf:protobuf-java-util:3.24.4")
  implementation("com.google.truth:truth:0.42")

  intellijPlatform {
    jetbrainsRuntime()
    intellijIdeaCommunity(defaultPluginRunMode.baseIDEVersion, useInstaller = false)

    defaultPluginRunMode.pluginDependencies.forEach {
      bundledPlugins(it)
    }
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }
}

sourceSets {
  main {
    kotlin.srcDirs(defaultPluginRunMode.sourcesDirs)
    java.srcDirs(defaultPluginRunMode.sourcesDirs)
    resources.srcDirs(defaultPluginRunMode.resourcesDirs)
  }
  test {
    kotlin.srcDirs(defaultPluginRunMode.testSourcesDirs)
    java.srcDirs(defaultPluginRunMode.testSourcesDirs)
    resources.srcDirs(defaultPluginRunMode.testResourcesDirs)
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
  }
}

tasks {
  val manipulatePluginXml by registering {
    dependsOn(named("processResources"))
    doLast {
      val fileToChange = file("build/resources/main/META-INF/plugin.xml")
      val newPluginXmlText =
        fileToChange.readText().replace(
          "(?s)<content\\b[^>]*>(.*?)</content>".toRegex(),
          """
                    <content>
                      ${defaultPluginRunMode.pluginXmlContents.joinToString(separator = "\n") { module -> "<module name=\"$module\"/>" }}
                    </content>
                    """.trimIndent()
        )
      fileToChange.writeText(newPluginXmlText)
    }
  }
  named("buildPlugin") {
    dependsOn(manipulatePluginXml)
  }
  named("runIde") {
    dependsOn(manipulatePluginXml)
  }
  named("test") {
    dependsOn(manipulatePluginXml)
  }
  test {
    systemProperty("ij.protoeditor.test.home.path", "${rootProject.rootDir}")
  }
  buildSearchableOptions {
    enabled = false
  }
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  wrapper {
    gradleVersion = ext("gradle.version")
  }
  runIde {
    autoReload.set(false)
  }
}

fun ext(name: String): String {
  return rootProject.extensions[name] as? String ?: error("Property `$name` is not defined")
}

sealed class ProtobufPluginLayout(
  val baseIDEVersion: String,
  val pluginDependencies: List<String>,
  val pluginXmlContents: List<String>,
  val sourcesDirs: Array<String>,
  val resourcesDirs: Array<String>,
  val testSourcesDirs: Array<String>,
  val testResourcesDirs: Array<String>
) {
  abstract class ProtobufCoreWithIjPlatform(
    baseIDEVersion: String,
    pluginDependencies: List<String>,
    pluginXmlContents: List<String>,
    sourcesDirs: Array<String>,
    resourcesDirs: Array<String>,
    testSourcesDirs: Array<String>,
    testResourcesDirs: Array<String>
  ) : ProtobufPluginLayout(
    baseIDEVersion,
    pluginDependencies,
    pluginXmlContents,
    sourcesDirs + arrayOf("protoeditor-core/src", "protoeditor-core/gen"),
    resourcesDirs + arrayOf("resources", "protoeditor-core/resources"),
    testSourcesDirs + arrayOf("protoeditor-core/test"),
    testResourcesDirs + arrayOf("protoeditor-core/testData")
  )

  class ProtobufCoreTestsInIdeaCommunity(majorIdeVersion: String) :
    ProtobufCoreWithIjPlatform(
      majorIdeVersion,
      listOf(
        "com.intellij.java",
      ),
      listOf("intellij.protoeditor.jvm"),
      arrayOf("protoeditor-jvm/src"),
      arrayOf("protoeditor-jvm/resources"),
      arrayOf("protoeditor-jvm/test"),
      arrayOf("protoeditor-jvm/testData")
    )
}