package org.jetbrains.qodana.jvm.coverage

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.junit.Test
import kotlin.io.path.writeText

@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
class QodanaScopedScriptRunnerTest : QodanaRunnerTestCase(){

  @Test
  fun testWithoutChangesData() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    System.setProperty(COVERAGE_DATA, getTestDataPath("coverage").toString())

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:JvmCoverageInspection"),
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty("qodana.skip.coverage.issues.reporting", "true")
      runAnalysis()
    } finally {
      System.clearProperty("qodana.skip.coverage.issues.reporting")
    }
    assertSarifResults()
  }

  @Test
  fun testXmlReport() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    System.setProperty(COVERAGE_DATA, getTestDataPath("coverage").toString())

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:JvmCoverageInspection"),
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ {
            "firstLine" : 5,
            "count" : 1
          }, 
          {
             "firstLine" : 11,
             "count" : 4
          }],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty("qodana.skip.coverage.issues.reporting", "true")
      runAnalysis()
    } finally {
      System.clearProperty("qodana.skip.coverage.issues.reporting")
    }
    assertSarifResults()
  }

  @Test
  fun testFirstStepComputesNothing() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    System.setProperty(COVERAGE_DATA, getTestDataPath("coverage").toString())

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:JvmCoverageInspection"),
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ {
            "firstLine" : 5,
            "count" : 1
          }, 
          {
             "firstLine" : 11,
             "count" : 4
          }],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty("qodana.skip.coverage.computation", "true")
      runAnalysis()
    } finally {
      System.clearProperty("qodana.skip.coverage.computation")
    }
    assertSarifResults()
  }
}