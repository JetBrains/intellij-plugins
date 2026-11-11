package com.intellij.lang.javascript.linter.eslint

import com.intellij.internal.statistic.FUCollectorTestCase
import com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.Companion.eslintServiceFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.fus.reporting.model.lion3.LogEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class ESLintEventCollectorTest {
  private val eslint by eslintServiceFixture(
    JSTempDirFixtureOptions.forBasePath("$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/event/")
      .withMainPackage("eslint")
      .withGlobalPackages(mapOf("eslint" to "9.28.0")),
    { EslintInspection() })

  @Test
  fun testSuccessfulResponse() {
    val event = collectEvent {
      eslint.doEditorHighlightingTest("test.js")
    }

    assertNotNull(event, "Expected event to be logged")
    assertEquals("js.eslint", event!!.group.id)
    assertEquals("eslint.response", event.event.id)
    assertEquals(ESLintEventCollector.ResponseStatus.SUCCESS.toString(), event.event.data["status"])
    assertTrue(event.event.data.containsKey("duration_ms"), "Expected response time to be logged")
  }

  @Test
  fun testTimeoutResponse() {
    // Deterministic timeout via a never-responding fake service (no real node process spawned). See
    // EslintServiceFixture.highlightWithNeverRespondingService (WEB-67172).
    eslint.fixture.copyDirectoryToProject(eslint.getTestName(false), "")
    val psiFile = eslint.fixture.configureByFile("test.js")
    JSLanguageServiceUtil.setTimeout(1, eslint.testRootDisposable)

    val event = collectEvent {
      eslint.highlightWithNeverRespondingService(psiFile)
    }

    assertNotNull(event, "Expected event to be logged")
    assertEquals("js.eslint", event!!.group.id)
    assertEquals("eslint.response", event.event.id)
    assertEquals(ESLintEventCollector.ResponseStatus.TIMEOUT.toString(), event.event.data["status"])
    assertTrue(event.event.data.containsKey("duration_ms"), "Expected response time to be logged")
  }

  private fun collectEvent(action: () -> Unit): LogEvent? {
    val events = FUCollectorTestCase.collectLogEvents(eslint.testRootDisposable, action)
    return events.singleOrNull { it.group.id == "js.eslint" }
  }
}
