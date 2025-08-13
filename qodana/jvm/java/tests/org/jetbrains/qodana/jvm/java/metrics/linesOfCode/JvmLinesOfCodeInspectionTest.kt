package org.jetbrains.qodana.jvm.java.metrics.linesOfCode

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/JvmLinesOfCodeInspectionTest")
class JvmLinesOfCodeInspectionTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "java", "test-data")

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl(), testRootDisposable)
    manager.registerEmbeddedProfilesTestProvider()
  }

  private fun updateConfig(
    includeInspections: List<InspectScope> = listOf(InspectScope("JvmLinesOfCodeInspection")),
    profile: QodanaProfileConfig = QodanaProfileConfig.named("empty")
  ) {
    updateQodanaConfig {
      it.copy(
        include = includeInspections,
        profile = profile
      )
    }
  }

  @Test
  fun testBlockCommentsOnTheSameLineAsCode(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testEmptyFile(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFileWithCodeOnly(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFileWithCommentsOnly(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFileWithDocComments(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFileWithLineComments(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFileWithWhitespacesOnly(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testLineCommentsOnTheSameLineAsCode(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testAllCodeOnTheSameLine(): Unit = runBlocking {
    updateConfig()
    runAnalysis()
    assertSarifResults()
  }

}