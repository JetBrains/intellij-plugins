package org.jetbrains.qodana.jvm.java.metrics.cyclomaticComplexity

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/JavaCyclomaticComplexityInspectionTest")
class JavaCyclomaticComplexityInspectionTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "java", "test-data")

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl(), testRootDisposable)
    manager.registerEmbeddedProfilesTestProvider()
  }

  @Test
  fun testAnonymousClasses() = executeTest()

  @Test
  fun testCatch() = executeTest()

  @Test
  fun testConstructor() = executeTest()

  @Test
  fun testDoWhile() = executeTest()

  @Test
  fun testFor() = executeTest()

  @Test
  fun testIf() = executeTest()

  @Test
  fun testLambdaFunctions() = executeTest()

  @Test
  fun testLogicalOrAnd() = executeTest()

  @Test
  fun testSwitchExpression() = executeTest()

  @Test
  fun testSwitchStatement() = executeTest()

  @Test
  fun testTernaryOperator() = executeTest()

  @Test
  fun testWhile() = executeTest()

  private fun executeTest() {
    runBlocking {
      setUpProfile()
      runAnalysis()
      assertSarifResults()
    }
  }

  private fun setUpProfile() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:CyclomaticComplexityInspection"),
      )
    }
  }
}