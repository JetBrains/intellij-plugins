package org.jetbrains.qodana.staticAnalysis.inspections.injections

import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath($$"$CONTENT_ROOT/core/test-data/DisabledCodeInjectionsTest")
class DisabledCodeInjectionsTest: QodanaRunnerTestCase() {

  @Test
  fun `html in js strings disabled`(): Unit = runBlocking {
    runAnalysis()
    assertSarifResults()
  }

}
