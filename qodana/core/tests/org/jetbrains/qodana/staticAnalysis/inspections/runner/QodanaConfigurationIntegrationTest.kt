package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.assertInstanceOf
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.script.CHANGES_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.LocalChangesScript
import org.jetbrains.qodana.staticAnalysis.script.TEAMCITY_CHANGES_SCRIPT_NAME
import org.junit.Test

class QodanaConfigurationIntegrationTest : QodanaConfigurationIntegrationBaseTest() {

  @Test
  fun `parse command line`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val projectFiles = listOf(
      "empty.xml" to emptyXML,
    )

    val cliArgs = listOf(
      "-c",
      "-d", "test",
      "-p", "$testProjectPath/empty.xml",
      "--fail-threshold", "15",
      "--disable-sanity",
      "--run-promo", "false",
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, runContext, config, app) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(CHANGES_SCRIPT_NAME, app.config.script.name) // from -c
    assertEquals("test", config.sourceDirectory)
    assertEquals(15, config.failureConditions.severityThresholds.any)
    assertEquals(false, config.runPromoInspections)
    assertEquals(true, config.disableSanityInspections)
    assertEquals("empty", runContext.baseProfile.name)
    assertEquals(true, script is LocalChangesScript)
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
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
      "empty.xml" to emptyXML,
      "conventions.xml" to conventionsXML
    )

    val cliArgs = listOf(
      "--changes",
      "-p", "$testProjectPath/conventions.xml",
      "--fail-threshold", "10",
      "--disable-sanity",
      "--run-promo", "false",
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, runContext, config, app) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(CHANGES_SCRIPT_NAME, app.config.script.name) // from --changes
    assertEquals(10, config.failureConditions.severityThresholds.any)
    assertEquals(false, config.runPromoInspections)
    assertEquals(true, config.disableSanityInspections)
    assertEquals("default.name.conventions", runContext.baseProfile.name)
    assertEquals(true, script is LocalChangesScript)
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
  fun `changes arg overrides default script`() = runTest {
    val testProjectPath = project.basePath

    val qodanaYAML = """
      version: 1.0
      script:
        name: default
      runPromoInspections: true
      disableSanityInspections: true
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
    )

    val cliArgs = listOf(
      "-c",
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, _, _, _) = buildScript(cliArgs, project, projectFiles, this)

    assertInstanceOf<LocalChangesScript>(script)
  }

  @Test
  fun `changes arg works with changes script`() = runTest {
    val testProjectPath = project.basePath

    val qodanaYAML = """
      version: 1.0
      script:
        name: $CHANGES_SCRIPT_NAME
      runPromoInspections: true
      disableSanityInspections: true
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
    )

    val cliArgs = listOf(
      "-c",
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, _, _, _) = buildScript(cliArgs, project, projectFiles, this)

    assertInstanceOf<LocalChangesScript>(script)
  }

  @Test
  fun `changes arg fails with other script`() {
    val testProjectPath = project.basePath

    val qodanaYAML = """
      version: 1.0
      script:
        name: $TEAMCITY_CHANGES_SCRIPT_NAME
      runPromoInspections: true
      disableSanityInspections: true
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
    )

    val cliArgs = listOf(
      "-c",
      "$testProjectPath",
      "$testProjectPath/out")

    assertThrows(QodanaException::class.java,
                 "Cannot combine '--changes' option with configured script '$TEAMCITY_CHANGES_SCRIPT_NAME' in yaml.") {
      runTest { buildScript(cliArgs, project, projectFiles, this) }
    }
  }
}
