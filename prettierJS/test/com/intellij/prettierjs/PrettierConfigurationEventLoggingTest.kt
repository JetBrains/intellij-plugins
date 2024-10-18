// PrettierEventLoggingTest.kt
package com.intellij.prettierjs

import com.intellij.internal.statistic.FUCollectorTestCase
import com.intellij.testFramework.runInEdtAndWait
import com.jetbrains.fus.reporting.model.lion3.LogEvent

class PrettierConfigurationEventLoggingTest : PrettierConfigurationTestBase() {

  fun testRootDirectoryAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "CONFIG_FILE"
    )
  )

  fun testRootDirectoryAutoPackageJson() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "PACKAGE_JSON"
    )
  )

  fun testRootDirectoryDisabled() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "UNCHANGED",
      "package_declaration_location" to "IN_PROJECT_ROOT_PACKAGE",
      "config_location" to "NONE"
    )
  )

  fun testSubPackageAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_SUBDIR_PACKAGE",
      "config_location" to "CONFIG_FILE"
    )
  )

  fun testSubPackageMultipleAuto() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_MULTIPLE_SUBDIR_PACKAGES",
      "config_location" to "CONFIG_FILE"
    )
  )

  fun testSubPackageMultipleAutoMixed() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "AUTOMATIC",
      "package_declaration_location" to "IN_MULTIPLE_SUBDIR_PACKAGES",
      "config_location" to "MIXED"
    )
  )

  fun testSubPackageDisabled() = doTest(
    expectedEventData = mapOf(
      "enabled_status" to "UNCHANGED",
      "package_declaration_location" to "IN_SUBDIR_PACKAGE",
      "config_location" to "NONE"
    )
  )

  fun testSubPackageDisabledNoPrettier() = doTest(
    expectedEventData = emptyMap()
  )

  private fun doTest(expectedEventData: Map<String, Any>) {
    val event = collectEvent {
      configureAndRun()
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
    val events = FUCollectorTestCase.collectLogEvents(testRootDisposable) { action() }
    return events.singleOrNull { it.group.id == "prettier.configuration" }
  }

  private fun assertEvent(event: LogEvent, data: Map<String, Any>) {
    val expected = listOf("auto.enable.in.new.project" to data.toSortedMap())
    val actual = listOf(event.event.id to event.event.data.toSortedMap())
    assertEquals("Event data mismatch.", expected, actual)
  }
}
