package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.junit.Test
import java.nio.file.Path

class QodanaConfigurationIntegrationTest : QodanaConfigurationIntegrationBaseTest() {

  @Test
  fun `parse command line`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val projectFiles = listOf(
      "empty.xml" to emptyXML,
    )

    val cliArgs = listOf(
      "-d", "test",
      "-p", "$testProjectPath/empty.xml",
      "--fail-threshold", "15",
      "--disable-sanity",
      "--run-promo", "false",
      "$testProjectPath",
      "$testProjectPath/out")

    val (_, runContext, config, _) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(Path.of("test"), config.onlyDirectory)
    assertEquals(15, config.failureConditions.severityThresholds.any)
    assertEquals(false, config.runPromoInspections)
    assertEquals(true, config.disableSanityInspections)
    assertEquals("empty", runContext.baseProfile.name)
  }

  @Test
  fun `command line overrides yaml`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val qodanaYAML = """
      version: 1.0
      profile:
        path: $testProjectPath/empty.xml
      failThreshold: -17
      runPromoInspections: true
      disableSanityInspections: true
      onlyDirectory: src
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
      "empty.xml" to emptyXML,
      "conventions.xml" to conventionsXML
    )

    val cliArgs = listOf(
      "-p", "$testProjectPath/conventions.xml",
      "--fail-threshold", "10",
      "--disable-sanity",
      "--run-promo", "false",
      "--source-directory", "src2",
      "$testProjectPath",
      "$testProjectPath/out")

    val (_, runContext, config, _) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(10, config.failureConditions.severityThresholds.any)
    assertEquals(false, config.runPromoInspections)
    assertEquals(true, config.disableSanityInspections)
    assertEquals("default.name.conventions", runContext.baseProfile.name)
    assertEquals(Path.of("src2"), config.onlyDirectory)
  }

  @Test
  fun `parse command line, --fail-threshold is not an integer`() = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val cliArgs = listOf(
      "-p", "$testProjectPath/wrong/empty.xml",
      "--fail-threshold", "one",
      "--disable-sanity",
      "$testProjectPath",
      "$testProjectPath/out")

    try {
      buildScript(cliArgs, testProject, listOf("empty.xml" to emptyXML), this)
      fail("Test should fail with NumberFormatException for not int fail-threshold")
    }
    catch (ignored: NumberFormatException) {
    }
  }

  @Test
  fun `local-changes script from yaml does not exist`() {
    val testProjectPath = project.basePath

    val qodanaYAML = """
      version: 1.0
      script:
        name: local-changes
      runPromoInspections: true
      disableSanityInspections: true
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
    )

    val cliArgs = listOf(
      "$testProjectPath",
      "$testProjectPath/out")

    assertThrows(QodanaException::class.java,
                 "Script 'local-changes' does not exist") {
      runTest { buildScript(cliArgs, project, projectFiles, this) }
    }
  }
}
