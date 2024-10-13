package org.jetbrains.qodana.staticAnalysis.script

import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class DefaultScriptFactoryTest : QodanaTestCase() {

  @Test
  fun `createScript no parameters`() = runTest {
    buildScript("default")
  }

  @Test
  fun `createScript unknown parameter`() = runTest {
    val e = assertThrows<QodanaException> {
      buildScript("default", "unknown" to "")
    }
    assertEquals("Script 'default' cannot handle parameter 'unknown'", e.message)
  }

  @Test
  fun `createScript unknown parameters`() = runTest {
    val e = assertThrows<QodanaException> {
      buildScript("default", "unknown" to "", "p2" to "", "p3" to "")
    }
    assertEquals("Script 'default' cannot handle parameters 'unknown, p2, p3'", e.message)
  }
}
