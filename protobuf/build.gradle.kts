import java.io.File
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

rootProject.extensions.add("gradle.version", "9.0")
rootProject.extensions.add("kotlin.jvmTarget", "25")
rootProject.extensions.add("java.sourceCompatibility", "25")
rootProject.extensions.add("java.targetCompatibility", "25")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))
rootProject.extensions.add("junit.version", "4.13.2")

/**
 * Initialize this property in a specific Gradle task to determine the plugin runtime layout
 * that is different for various compatible IDEs
 */
val defaultPluginRunMode = ProtobufPluginLayout.ProtobufCoreWithJavaAndTestsInIdeaCommunity("2026.2.0.1")

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

  mavenCentral()
  google()
}

intellijPlatform {
  pluginConfiguration {
    name = "protobuf"
  }
}

dependencies {
  intellijPlatform {
    jetbrainsRuntime()
    intellijIdea(defaultPluginRunMode.baseIDEVersion)

    defaultPluginRunMode.pluginDependencies.forEach {
      bundledPlugins(it)
    }
    defaultPluginRunMode.moduleDependencies.forEach {
      bundledModule(it)
    }
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
    testFramework(TestFrameworkType.Plugin.Kotlin)
  }

  implementation("com.google.protobuf:protobuf-java-util:3.24.4")
  testImplementation("com.google.truth:truth:0.42")
  compileOnly("org.jetbrains:annotations:26.1.0")
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

java {
  sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
  targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
    freeCompilerArgs.add("-Xannotation-default-target=param-property")
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
                    <content namespace="jetbrains">
                      ${defaultPluginRunMode.pluginXmlContents.joinToString(separator = "\n") { module -> "<module name=\"$module\"/>" }}
                    </content>
                    """.trimIndent()
        )
      fileToChange.writeText(newPluginXmlText)
    }
  }
  buildPlugin {
    dependsOn(manipulatePluginXml)
  }
  test {
    dependsOn(manipulatePluginXml)
    systemProperty("ij.protoeditor.test.home.path", "${rootProject.rootDir}")
    systemProperty(
      "vfs.additional-allowed-roots",
      listOf(rootProject.rootDir, gradle.gradleUserHomeDir, rootProject.file(".intellijPlatform"))
        .joinToString(File.pathSeparator) { it.absolutePath }
    )
    useJUnit()
  }
  buildSearchableOptions {
    enabled = false
  }
  wrapper {
    gradleVersion = ext("gradle.version")
  }
  runIde {
    dependsOn(manipulatePluginXml)
    autoReload.set(false)
  }
  named("jar") {
    dependsOn(manipulatePluginXml)
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
      listOf("com.intellij.java", "org.jetbrains.kotlin", "tanvd.grazi"),
      listOf("intellij.platform.structureView", "intellij.spellchecker"),
      listOf("intellij.protoeditor.jvm", "intellij.protoeditor.kotlin"),
      arrayOf("protoeditor-jvm/src", "protoeditor-kotlin/src", "protoeditor-core/src", "protoeditor-core/gen"),
      arrayOf("protoeditor-jvm/resources", "protoeditor-kotlin/resources", "resources", "protoeditor-core/resources"),
      arrayOf("protoeditor-jvm/test", "protoeditor-core/test", "protoeditor-kotlin/test"),
      arrayOf("protoeditor-jvm/testData", "protoeditor-core/testData", "protoeditor-kotlin/testData")
    )
}
