package org.jetbrains.qodana.jvm.kotlin.metrics.cyclomaticComplexity

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/KotlinCyclomaticComplexityInspectionTest")
class KotlinCyclomaticComplexityInspectionTest: QodanaRunnerTestCase() {

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl(), testRootDisposable)
    manager.registerEmbeddedProfilesTestProvider()
  }

  @Test
  fun testAnonymousObjects() = executeTest()

  @Test
  fun testCatch() = executeTest()

  @Test
  fun testClassInitializer() = executeTest()

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
  fun testNestedClasses() = executeTest()

  @Test
  fun testNestedFunctions() = executeTest()

  @Test
  fun testSafeCall() = executeTest()

  @Test
  fun testSafeCast() = executeTest()

  @Test
  fun testSecondaryConstructor() = executeTest()

  @Test
  fun testWhen() = executeTest()

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
      it.copy(profile = QodanaProfileConfig.named("qodana.single:CyclomaticComplexityInspection"))
    }
  }
}