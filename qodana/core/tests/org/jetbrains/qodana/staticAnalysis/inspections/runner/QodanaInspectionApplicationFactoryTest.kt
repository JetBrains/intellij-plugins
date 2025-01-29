package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.HeavyPlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlFiles
import org.jetbrains.qodana.staticAnalysis.script.CHANGES_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.TEAMCITY_CHANGES_SCRIPT_NAME
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

@RunWith(JUnit4::class)
class QodanaInspectionApplicationFactoryTest : HeavyPlatformTestCase() {
  private lateinit var projectFile: Path

  override fun setUp() {
    super.setUp()
    projectFile = Files.createFile(Path("PROJECT_PATH"))
  }

  override fun tearDown() {
    try {
      projectFile.deleteIfExists()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }
  @Test
  fun `long options`(): Unit = runBlocking {
    val args = listOf(
      "--profile-name", "NAMENAMENAME",
      "--changes",
      "--profile-path", "PATH/profile.xml",
      "--source-directory", "PROJECT_PATH/src",
      "--baseline", "/home/user/baseline/qodana.sarif.json",
      "--baseline-include-absent",
      "--disable-sanity",
      "--fail-threshold", "1000",
      "--run-promo", "false",
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)!!

    assertEquals("NAMENAMENAME", app.config.profile.name)
    assertEquals("PATH/profile.xml", app.config.profile.path)
    assertEquals("command line", app.config.profileSource)
    assertEquals(Path.of("PROJECT_PATH/").toAbsolutePath().pathString, app.config.projectPath.toString())
    assertEquals("/OUT_PATH", app.config.outPath.invariantSeparatorsPathString)
    assertEquals("PROJECT_PATH/src", app.config.sourceDirectory)
    assertEquals(CHANGES_SCRIPT_NAME, app.config.script.name) // from --changes
    assertEquals("/home/user/baseline/qodana.sarif.json", app.config.baseline)
    assertEquals(true, app.config.includeAbsent)
    assertTrue(app.config.disableSanityInspections)
    assertEquals(1000, app.config.failureConditions.severityThresholds.any)
    assertEquals(false, app.config.runPromoInspections)
  }

  @Test
  fun `short options`(): Unit = runBlocking {
    val args = listOf(
      "-n", "NAMENAMENAME",
      "-c",
      "-p", "PATH/profile.xml",
      "-d", "PROJECT_PATH/src",
      "-b", "/home/user/baseline/qodana.sarif.json",
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)!!

    assertEquals("NAMENAMENAME", app.config.profile.name)
    assertEquals("PATH/profile.xml", app.config.profile.path)
    assertEquals("command line", app.config.profileSource)
    assertEquals(Path.of("PROJECT_PATH/").toAbsolutePath().pathString, app.config.projectPath.toString())
    assertEquals("/OUT_PATH", app.config.outPath.invariantSeparatorsPathString)
    assertEquals("PROJECT_PATH/src", app.config.sourceDirectory)
    assertEquals(CHANGES_SCRIPT_NAME, app.config.script.name) // from -c
    assertEquals("/home/user/baseline/qodana.sarif.json", app.config.baseline)
  }

  @Test
  fun `empty options`(): Unit = runBlocking {
    val args = listOf(
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)!!

    assertEquals("", app.config.profile.name)
    assertEquals("", app.config.profile.path)
    assertEquals(QODANA_YAML_CONFIG_FILENAME, app.config.profileSource)
    assertEquals(Path.of("PROJECT_PATH/").toAbsolutePath().pathString, app.config.projectPath.toString())
    assertEquals("/OUT_PATH", app.config.outPath.invariantSeparatorsPathString)
    assertEquals(null, app.config.sourceDirectory)

    assertNull(app.config.yamlFiles.effectiveQodanaYaml)
    assertNull(app.config.yamlFiles.localQodanaYaml)
    assertNull(app.config.yamlFiles.qodanaConfigJson)

    assertEquals(
      QodanaConfig.fromYaml(
        Path.of("PROJECT_PATH/").toAbsolutePath(),
        Path.of("/OUT_PATH")),
      app.config)
  }

  @Test
  fun `canonicalise project path`(): Unit = runBlocking {
    val args = listOf(
      "./PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)!!
    assertEquals(Path.of("PROJECT_PATH/").toAbsolutePath().toString(), app.config.projectPath.toString())
  }

  @Test
  fun `deprecated options`(): Unit = runBlocking {
    val args = listOf(
      "-profileName", "NAMENAMENAME",
      "-changes",
      "-profilePath", "PATH/profile.xml",
      "--stub-profile", "this/should/be/ignored",
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)!!

    assertEquals("NAMENAMENAME", app.config.profile.name)
    assertEquals("PATH/profile.xml", app.config.profile.path)
    assertEquals("command line", app.config.profileSource)
    assertEquals(Path.of("PROJECT_PATH/").toAbsolutePath(), app.config.projectPath)
    assertEquals("/OUT_PATH", app.config.outPath.invariantSeparatorsPathString)
    assertEquals(CHANGES_SCRIPT_NAME, app.config.script.name) // from -c
  }

  @Test
  fun `unknown option`(): Unit = runBlocking {
    val args = listOf(
      "--profile-name", "NAMENAMENAME",
      "--smth", "unknown",
      "--profile-path", "PATH/profile.xml",
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val e = assertThrows<QodanaException> {
      QodanaInspectionApplicationFactory().buildApplication(args)
    }
    // TODO: Use a better command line argument parser.
    //  Or at least make the error message more specific:
    //  Unknown option '--smth'.
    assertEquals("Arguments should contain only PROJECT_PATH and RESULTS_PATH. " +
                 "Arguments: [--smth, unknown, --profile-path, PATH/profile.xml, PROJECT_PATH/, /OUT_PATH]",
                 e.message)
  }

  @Test
  fun `too many arguments`(): Unit = runBlocking {
    val args = listOf(
      "--profile-name", "NAMENAMENAME",
      "--profile-path", "PATH/profile.xml",
      "PROJECT_PATH/",
      "TOO_MANY",
      "/OUT_PATH",
    )

    val e = assertThrows<QodanaException> {
      QodanaInspectionApplicationFactory().buildApplication(args)
    }
    assertEquals("Arguments should contain only PROJECT_PATH and RESULTS_PATH. " +
                 "Arguments: [PROJECT_PATH/, TOO_MANY, /OUT_PATH]",
                 e.message)
  }

  @Test
  fun `combining non-default script with changes should fail`(): Unit = runBlocking {
    val args = listOf(
      "--changes",
      "--script", TEAMCITY_CHANGES_SCRIPT_NAME,
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val e = assertThrows<QodanaException> {
      QodanaInspectionApplicationFactory().buildApplication(args)
    }
    assertEquals("Cannot combine '--script' option with '--changes'. Consider using '--script local-changes' instead of '--changes'",
                 e.message)
  }

  @Test
  fun `combining local-changes script with changes should not fail`(): Unit = runBlocking {
    val args = listOf(
      "--changes",
      "--script", CHANGES_SCRIPT_NAME,
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)

    assertEquals(app?.config?.script?.name, CHANGES_SCRIPT_NAME)
  }


  @Test
  fun `custom non-existent qodana yaml`(): Unit = runBlocking {
    val args = listOf(
      "--config", "custom.qodana.yaml",
      "PROJECT_PATH/",
      "/OUT_PATH",
    )

    assertThrows<QodanaException> { QodanaInspectionApplicationFactory().buildApplication(args)}
  }

  @Test
  fun `custom qodana yaml`(): Unit = runBlocking {
    val testProjectPath = Paths.get(project.basePath!!).resolve("custom.qodana.yaml")
    Paths.get(project.basePath!!).createDirectories()

    testProjectPath.writeText("""
      profile:
        name: empty
    """.trimIndent(), Charsets.UTF_8, StandardOpenOption.CREATE)

    val args = listOf(
      "--config", "custom.qodana.yaml",
      "${project.basePath}",
      "/OUT_PATH",
    )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)
    assertEquals(testProjectPath, app?.config?.yamlFiles?.effectiveQodanaYaml)
    assertEquals("empty", app?.config?.profile?.name)
  }

  @Test
  fun `effective qodana yaml is applied`(): Unit = runBlocking {
    val configDir = Paths.get(project.basePath!!).resolve(".qodana/config")
    configDir.createDirectories()

    @Language("yaml")
    val localQodanaYaml = """
      version: 1.0
      
      profile:
        name: empty
    """.trimIndent()
    val localQodanaYamlPath = configDir.resolve(QodanaYamlFiles.LOCAL_QODANA_YAML_FILENAME)
    localQodanaYamlPath.writeText(localQodanaYaml)

    val profileYaml = configDir.resolve("profile.yaml").createFile()

    @Language("yaml")
    val effectiveQodanaYaml = """
      version: 1.0
      
      bootstrap: 'bootstrap'

      profile:
        name: qodana.recommended
        path: profile.yaml
    """.trimIndent()
    val effectiveQodanaYamlPath = configDir.resolve(QodanaYamlFiles.EFFECTIVE_QODANA_YAML_FILENAME)
    effectiveQodanaYamlPath.writeText(effectiveQodanaYaml)

    val args = listOf(
        "--config-dir", ".qodana/config",
        "${project.basePath}",
        "/OUT_PATH",
      )

    val app = QodanaInspectionApplicationFactory().buildApplication(args)
    assertThat(app?.config?.profile?.name).isEqualTo("qodana.recommended")
    assertThat(app?.config?.profile?.path).isEqualTo(profileYaml.pathString)
    assertThat(app?.config?.bootstrap).isEqualTo("bootstrap")

    val yamlFiles = app?.config?.yamlFiles
    assertThat(yamlFiles?.effectiveQodanaYaml).isEqualTo(effectiveQodanaYamlPath)
    assertThat(yamlFiles?.localQodanaYaml).isEqualTo(localQodanaYamlPath)
  }
}
