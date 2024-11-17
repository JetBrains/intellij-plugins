package org.jetbrains.qodana.staticAnalysis.inspections.injections

import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/DisabledCodeInjectionsTest")
class DisabledCodeInjectionsTest: QodanaRunnerTestCase() {

  @Test
  fun `html in js strings disabled`(): Unit = runBlocking {
    runAnalysis()
    assertSarifResults()
  }

}
