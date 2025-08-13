package org.jetbrains.qodana.jvm.kotlin

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data")
class KotlinCoverageTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "kotlin", "test-data")

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
        profile = QodanaProfileConfig.named("qodana.single:JvmCoverageInspection"),
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