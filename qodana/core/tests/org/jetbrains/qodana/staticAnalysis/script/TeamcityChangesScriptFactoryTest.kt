package org.jetbrains.qodana.staticAnalysis.script

import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.junit.Test

class TeamcityChangesScriptFactoryTest : QodanaTestCase() {

  @Test
  fun `parseParameters without path`() {
    val factory = TeamCityChangesScriptFactory()

    val parameters = factory.parseParameters("")

    assertEquals(mapOf<String, String>(), parameters)
  }

  @Test
  fun `parseParameters with path`() {
    val factory = TeamCityChangesScriptFactory()

    val parameters = factory.parseParameters("/path/to/project")

    assertEquals(mapOf("path" to "/path/to/project"), parameters)
  }
}
