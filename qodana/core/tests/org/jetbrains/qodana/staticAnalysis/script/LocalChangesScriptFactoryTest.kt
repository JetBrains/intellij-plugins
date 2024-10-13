package org.jetbrains.qodana.staticAnalysis.script

import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.junit.Test

class LocalChangesScriptFactoryTest : QodanaTestCase() {
  @Test
  fun `parse empty parameters`() {
    val scriptFactory = LocalChangesScriptFactory()
    val parameters = scriptFactory.parseParameters("")
    assertEquals(mapOf<String, String>(), parameters)
  }

  @Test
  fun `parse nonempty parameters`() {
    val scriptFactory = LocalChangesScriptFactory()
    assertThrows(QodanaException::class.java, "The 'local-changes' script does not take parameters") {
      scriptFactory.parseParameters("nonempty")
    }
  }

  @Test
  fun `create script no parameters`() = runTest {
    buildScript("local-changes")
  }

  @Test
  fun `create script nonempty parameters`() = runTest {
    assertThrows(QodanaException::class.java, "Script 'local-changes' cannot handle parameter 'param'") {
      buildScript("local-changes", "param" to "value")
    }
  }
}
