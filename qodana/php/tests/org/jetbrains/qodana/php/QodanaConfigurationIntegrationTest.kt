package org.jetbrains.qodana.php

import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaConfigurationIntegrationBaseTest
import org.jetbrains.qodana.staticAnalysis.inspections.runner.baseProfile
import org.junit.Test

class QodanaConfigurationIntegrationTest : QodanaConfigurationIntegrationBaseTest() {
  @Test
  fun `load yaml file`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val qodanaYAML = """
      version: 1.0
      profile:
        path: $testProjectPath/empty.xml
      failThreshold: 13
      disableSanityInspections: true
      script:
        name: php-migration
        parameters:
          fromLevel: 7.1
          toLevel: 8.0
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
      "empty.xml" to emptyXML,
    )

    val cliArgs = listOf(
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, runContext, config, _) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(13, config.failureConditions.severityThresholds.any)
    assertEquals(true, config.disableSanityInspections)
    assertEquals("empty", runContext.baseProfile.name)
    assertEquals(true, script is PhpMigrationScript)
    assertEquals("7.1", config.script.parameters["fromLevel"].toString())
    assertEquals("8.0", config.script.parameters["toLevel"].toString())
  }

  @Test
  fun `load yaml file with 3 part php level`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val qodanaYAML = """
      version: 1.0
      profile:
        path: $testProjectPath/empty.xml
      script:
        name: php-migration
        parameters:
          fromLevel: 5.3.0
          toLevel: 8.0
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
      "empty.xml" to emptyXML,
    )

    val cliArgs = listOf(
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, _, config, _) = buildScript(cliArgs, testProject, projectFiles, this)

    assertEquals(true, script is PhpMigrationScript)
    assertEquals("5.3.0", config.script.parameters["fromLevel"].toString())
    assertEquals("8.0", config.script.parameters["toLevel"].toString())
  }

  @Test
  fun `php-migration via command line`(): Unit = runTest {
    val testProject = project
    val testProjectPath = testProject.basePath

    val projectFiles = listOf(
      "empty.xml" to emptyXML,
    )

    val cliArgs = listOf(
      "--script", "php-migration:7.2-to-8.1",
      //TODO: QD-2598 --source-directory is treated like an absolute path
      //"--source-directory", "src",
      "-p", "$testProjectPath/empty.xml",
      "--disable-sanity",
      "$testProjectPath",
      "$testProjectPath/out")

    val (script, _, config, app) = buildScript(cliArgs, testProject, projectFiles, this)

    //assertEquals("src", app.mySourceDirectory)
    assertEquals(true, script is PhpMigrationScript)
    assertEquals("7.2", config.script.parameters["fromLevel"])
    assertEquals("8.1", config.script.parameters["toLevel"])
  }
}
