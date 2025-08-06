import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "../contrib-configuration/common.gradle.kts")

/**
 * Initialize this property in a specific Gradle task to determine the plugin runtime layout
 * that is different for various compatible IDEs
 */
val defaultPluginRunMode = ProtobufPluginLayout.ProtobufCoreWithJavaAndTestsInIdeaCommunity("2025.2")

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
  intellijPlatform {
    jetbrainsRuntime()
    intellijIdeaCommunity(defaultPluginRunMode.baseIDEVersion, useInstaller = true)

    defaultPluginRunMode.pluginDependencies.forEach {
      bundledPlugins(it)
    }
    defaultPluginRunMode.moduleDependencies.forEach {
      bundledModule(it)
    }
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  implementation("com.google.protobuf:protobuf-java-util:3.24.4")
  implementation("com.google.truth:truth:0.42")
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
    useJUnit()
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
  val moduleDependencies: List<String>,
  val pluginXmlContents: List<String>,
  val sourcesDirs: Array<String>,
  val resourcesDirs: Array<String>,
  val testSourcesDirs: Array<String>,
  val testResourcesDirs: Array<String>
) {
  class ProtobufCoreWithJavaAndTestsInIdeaCommunity(majorIdeVersion: String) :
    ProtobufPluginLayout(
      majorIdeVersion,
      listOf("com.intellij.java"),
      listOf("intellij.spellchecker"),
      listOf("intellij.protoeditor.jvm"),
      arrayOf("protoeditor-jvm/src", "protoeditor-core/src", "protoeditor-core/gen"),
      arrayOf("protoeditor-jvm/resources", "resources", "protoeditor-core/resources"),
      arrayOf("protoeditor-jvm/test", "protoeditor-core/test"),
      arrayOf("protoeditor-jvm/testData", "protoeditor-core/testData")
    )
}