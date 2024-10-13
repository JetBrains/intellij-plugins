package org.jetbrains.qodana.jvm.kotlin

import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/../../core/test-data/KotlinCoverageTest")
class KotlinCoverageTest : QodanaRunnerTestCase() {

  @Before
  fun before() {
    val coveragePath = getTestDataPath("coverage").toString()
    System.setProperty("qodana.coverage.input", coveragePath)
  }

  @After
  fun after() {
    System.clearProperty("qodana.coverage.input")
  }


  @Test
  fun testKotlin(): Unit = runTest {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:JvmCoverageInspection"),
        disableSanityInspections = true,
        coverage = it.coverage.copy(
          reportProblems = true
        )
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}