package org.jetbrains.qodana.php

import com.jetbrains.php.config.PhpLanguageLevel
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.script.buildScript
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class PhpMigrationScriptFactoryTest : QodanaTestCase() {

  @Test
  fun `parseParameters good`() {
    val factory = PhpMigrationScriptFactory()

    val parameters = factory.parseParameters("5.6-to-7.4")

    val expected = mapOf(
      "fromLevel" to "5.6",
      "toLevel" to "7.4")
    assertEquals(expected, parameters)
  }

  @Test
  fun `parseParameters empty`() {
    val factory = PhpMigrationScriptFactory()

    val e = assertThrows<QodanaException> {
      factory.parseParameters("")
    }
    assertEquals("CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
                 "For example '--script php-migration:7.1-to-8.0'.",
                 e.message)
  }

  @Test
  fun `parseParameters missing separator`() {
    val factory = PhpMigrationScriptFactory()

    val e = assertThrows<QodanaException> {
      factory.parseParameters("8.0")
    }
    assertEquals("CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
                 "For example '--script php-migration:7.1-to-8.0'.",
                 e.message)
  }

  @Test
  fun `parseParameters too many versions`() {
    val factory = PhpMigrationScriptFactory()

    val e = assertThrows<QodanaException> {
      factory.parseParameters("5.6-to-7.4-to-8.0")
    }
    assertEquals("CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
                 "For example '--script php-migration:7.1-to-8.0'.",
                 e.message)
  }

  @Test
  fun `parseParameters empty fromVersion`() {
    val factory = PhpMigrationScriptFactory()

    val e = assertThrows<QodanaException> {
      factory.parseParameters("-to-7.4")
    }
    assertEquals("CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
                 "For example '--script php-migration:7.1-to-8.0'.",
                 e.message)
  }

  @Test
  fun `parseParameters empty toVersion`() {
    val factory = PhpMigrationScriptFactory()

    val e = assertThrows<QodanaException> {
      factory.parseParameters("5.6-to-")
    }
    assertEquals("CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
                 "For example '--script php-migration:7.1-to-8.0'.",
                 e.message)
  }

  @Test
  fun `createScript empty parameters`() = runTest {
    val e = assertThrows<QodanaException> {
      buildScript("php-migration")
    }
    assertEquals("Script 'php-migration' requires parameter 'fromLevel'", e.message)
  }

  @Test
  fun `createScript good`() = runTest {
    buildScript("php-migration", "fromLevel" to "7.4", "toLevel" to "8.0")
  }

  @Test
  fun `createScript too many parameters`() = runTest {
    val e = assertThrows<QodanaException> {
      buildScript("php-migration", "fromLevel" to "7.4", "toLevel" to "8.0", "unknown" to "")
    }
    assertEquals("Script 'php-migration' cannot handle parameter 'unknown'", e.message)
  }

  @Test
  fun `createScript unknown PHP version 3_0`() = runTest {
    val e = assertThrows<QodanaException> {
      buildScript("php-migration", "fromLevel" to "3.0", "toLevel" to "8.0")
    }
    assertEquals(
      "Unknown PHP language level '3.0', " +
      "use one of ${PhpLanguageLevel.values().joinToString(", ") { it.versionString }}",
      e.message)
  }

  @Test
  fun `createScript default PHP version`() = runTest {
    // The default PHP version must be accepted if it is written correctly.
    assertEquals(PhpLanguageLevel.DEFAULT.versionString, "5.6.0")
    buildScript("php-migration", "fromLevel" to PhpLanguageLevel.DEFAULT.versionString, "toLevel" to "8.0")
  }

  @Test
  fun `createScript unknown PHP version 5_6`() = runTest {
    // There is no PHP language level with the version string '5.6',
    // even though there is one that has '5.6' as its 'presentable name'.
    assertEquals(PhpLanguageLevel.DEFAULT.versionString, "5.6.0")
    assertEquals(PhpLanguageLevel.DEFAULT.presentableName, "5.6")

    val e = assertThrows<QodanaException> {
      buildScript("php-migration", "fromLevel" to "5.6", "toLevel" to "8.0")
    }
    assertEquals("Unknown PHP language level '5.6', " +
                 "use one of ${PhpLanguageLevel.values().joinToString(", ") { it.versionString }}",
                 e.message)
  }
}
