package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.testFramework.HeavyPlatformTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QodanaScriptFactoryTest : HeavyPlatformTestCase() {

  @Test
  fun `parseParameters default`() {
    val config = QodanaScriptFactory.parseConfigFromArgument("default")!!

    assertEquals(QodanaScriptConfig("default"), config)
  }

  @Test
  fun `parseParameters default with missing parameters`() {
    val e = assertThrows<QodanaException> {
      QodanaScriptFactory.parseConfigFromArgument("default:")
    }
    assertEquals("Script parameters in '--script default:' must not be empty", e.message)
  }

  @Test
  fun `parseParameters default with some parameters`() {
    val e = assertThrows<QodanaException> {
      QodanaScriptFactory.parseConfigFromArgument("default:parameters")
    }
    assertEquals("The 'default' script does not take parameters", e.message)
  }

  /** The name 'default-script' is not known; the name 'default' would be known, but it is not followed by a ':'. */
  @Test
  fun `parseParameters longer name`() {
    val config = QodanaScriptFactory.parseConfigFromArgument("default-script:parameters")

    assertEquals(null, config)
  }

  // Script names must be spelled out, they cannot be abbreviated.
  @Test
  fun `parseParameters abbreviated no parameters`() {
    val config = QodanaScriptFactory.parseConfigFromArgument("def")

    assertEquals(null, config)
  }

  // Script names must be spelled out, they cannot be abbreviated.
  @Test
  fun `parseParameters abbreviated some parameters`() {
    val config = QodanaScriptFactory.parseConfigFromArgument("def:parameters")

    assertEquals(null, config)
  }
}
