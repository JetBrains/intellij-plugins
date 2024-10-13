package org.jetbrains.qodana.jvm.java.metrics.cyclomaticComplexity

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/JavaCyclomaticComplexityInspectionTest")
class JavaCyclomaticComplexityInspectionTest : QodanaRunnerTestCase() {

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl(), testRootDisposable)
    manager.registerEmbeddedProfilesTestProvider()
  }

  @Test
  fun testAnonymousClasses(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testCatch(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testConstructor(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testDoWhile(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testFor(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testIf(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testLambdaFunctions(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testLogicalOrAnd(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testSwitchExpression(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testSwitchStatement(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testTernaryOperator(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testWhile(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(profile = QodanaProfileConfig(name = "qodana.single:CyclomaticComplexityInspection"))
    }
    runAnalysis()
    assertSarifResults()
  }

}