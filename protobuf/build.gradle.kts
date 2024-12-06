apply(from = "../contrib-configuration/common.gradle.kts")

/**
 * Initialize this property in a specific Gradle task to determine the plugin runtime layout
 * that is different for various compatible IDEs
 */
//val defaultPluginRunMode = ProtobufPluginLayout.ProtobufInIdeaUltimateWithGoAndPython("2024.1", "241.14494.240")
val defaultPluginRunMode = ProtobufPluginLayout.ProtobufCoreTestsInIdeaCommunity("2024.3")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

dependencies {
  implementation("com.google.protobuf:protobuf-java-util:3.24.4")
  implementation("com.google.truth:truth:0.42")
}

intellij {
  pluginName.set("protobuf")
  type.set(defaultPluginRunMode.baseIDE)
  version.set(defaultPluginRunMode.baseIDEVersion)
  plugins.set(defaultPluginRunMode.pluginDependencies)
}

sourceSets {
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
  compileKotlin {
    kotlinOptions.jvmTarget = ext("kotlin.jvmTarget")
    @Suppress("UNCHECKED_CAST")
    kotlinOptions.freeCompilerArgs = rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>
  }
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  wrapper {
    gradleVersion = "8.5"
  }
  runIde {
    autoReloadPlugins.set(false)
  }
}

fun ext(name: String): String {
  return rootProject.extensions[name] as? String ?: error("Property `$name` is not defined")
}

sealed class ProtobufPluginLayout(
  val baseIDE: String,
  val baseIDEVersion: String,
  val pluginDependencies: List<String>,
  val pluginXmlContents: List<String>,
  val sourcesDirs: Array<String>,
  val resourcesDirs: Array<String>,
  val testSourcesDirs: Array<String>,
  val testResourcesDirs: Array<String>
) {
  abstract class ProtobufCoreWithIjPlatform(
    baseIDE: String,
    baseIDEVersion: String,
    pluginDependencies: List<String>,
    pluginXmlContents: List<String>,
    sourcesDirs: Array<String>,
    resourcesDirs: Array<String>,
    testSourcesDirs: Array<String>,
    testResourcesDirs: Array<String>
  ) : ProtobufPluginLayout(
    baseIDE,
    baseIDEVersion,
    pluginDependencies,
    pluginXmlContents,
    sourcesDirs + arrayOf("protoeditor-core/src", "protoeditor-core/gen"),
    resourcesDirs + arrayOf("resources", "protoeditor-core/resources"),
    testSourcesDirs + arrayOf("protoeditor-core/test"),
    testResourcesDirs + arrayOf("protoeditor-core/testData")
  )

  class ProtobufInIdeaUltimateWithGoAndPython(majorIdeVersion: String, latestCompatiblePluginsVersion: String) :
    ProtobufCoreWithIjPlatform(
      "IU",
      majorIdeVersion,
      listOf(
        "com.intellij.java",
        "org.jetbrains.plugins.go:$latestCompatiblePluginsVersion",
        "Pythonid:$latestCompatiblePluginsVersion"
      ),
      listOf("intellij.protoeditor.go", "intellij.protoeditor.python", "intellij.protoeditor.jvm"),
      arrayOf("protoeditor-jvm/src"),
      arrayOf("protoeditor-jvm/resources"),
      arrayOf("protoeditor-jvm/test"),
      arrayOf("protoeditor-jvm/testData")
    )

  class ProtobufCoreTestsInIdeaCommunity(majorIdeVersion: String) :
    ProtobufCoreWithIjPlatform(
      "IC",
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