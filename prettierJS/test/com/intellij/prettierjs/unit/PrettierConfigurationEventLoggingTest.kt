// PrettierEventLoggingTest.kt
package com.intellij.prettierjs.unit

import com.intellij.internal.statistic.FUCollectorTestCase
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.jetbrains.fus.reporting.model.lion3.LogEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class PrettierConfigurationEventLoggingTest {
  private val prettier by prettierConfigurationFixture()

  @Test
  fun testRootDirectoryAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "CONFIG_FILE"
    )
  )

  @Test
  fun testRootDirectoryAutoPackageJson() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "PACKAGE_JSON"
    )
  )

  @Test
  fun testRootDirectoryDisabled() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "UNCHANGED",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "NONE"
    )
  )

  @Test
  fun testSubPackageAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_SUBDIR_PACKAGE",
      "config_location" to "CONFIG_FILE"
    )
  )

  @Test
  fun testSubPackageMultipleAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_MULTIPLE_SUBDIR_PACKAGES",
      "config_location" to "CONFIG_FILE"
    )
  )

  @Test
  fun testSubPackageMultipleAutoMixed() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_MULTIPLE_SUBDIR_PACKAGES",
      "config_location" to "MIXED"
    )
  )

  @Test
  fun testRootDependencyNestedConfig() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "CONFIG_FILE"
    )
  )

  @Test
  fun testRootDependencyNestedPackageJsonConfig() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "PACKAGE_JSON"
    )
  )

  @Test
  fun testSubPackageDisabled() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "UNCHANGED",
      "package_declaration_location" to "IN_SUBDIR_PACKAGE",
      "config_location" to "NONE"
    )
  )

  @Test
  fun testSubPackageDisabledNoPrettier() = doTest(
    expectedEventData = emptyMap()
  )

  private fun doTest(expectedEventData: Map<String, Any>) {
    val event = collectEvent {
      prettier.configureAndRun()
    }

    runInEdtAndWait {
      if (expectedEventData.isNotEmpty()) {
        assertNotNull(event)
        event?.let { assertEvent(it, expectedEventData) }
      }
      else {
        assertNull(event)
      }
    }
  }

  private fun collectEvent(action: () -> Unit): LogEvent? {
    val events = FUCollectorTestCase.collectLogEvents(prettier.fixture.testRootDisposable) { action() }
    return events.singleOrNull { it.group.id == "prettier.configuration" }
  }

  private fun assertEvent(event: LogEvent, data: Map<String, Any>) {
    val expected = listOf("auto.enable.in.new.project" to data.toSortedMap())
    val actual = listOf(event.event.id to event.event.data.toSortedMap())
    assertEquals(expected, actual, "Event data mismatch.")
  }
}
