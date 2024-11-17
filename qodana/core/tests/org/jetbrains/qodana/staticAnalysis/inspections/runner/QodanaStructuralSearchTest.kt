package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.TestDataPath
import org.junit.Test


@TestDataPath("\$CONTENT_ROOT/testData/QodanaStructuralSearchTest")
class QodanaStructuralSearchTest : QodanaRunnerTestCase(){
  @Test
  fun testProfileWithDisabledRule() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testProfileWithDisabledRuleAndExclude() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludePaths() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludeByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testExcludePathByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testIncludeByUUID() {
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun testIncludePathByUUID() {
    runAnalysis()
    assertSarifResults()
  }
}