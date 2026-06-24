package com.intellij.lang.javascript.linter.eslint

import com.intellij.internal.statistic.FUCollectorTestCase
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.jetbrains.fus.reporting.model.lion3.LogEvent
import org.junit.Assert
import kotlin.collections.singleOrNull

class ESLintEventCollectorTest : EslintServiceTestBase() {

  override fun getBasePath(): String {
    return ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/event/"
  }

  override fun getPackageName(): String {
    return "eslint"
  }

  override fun getInspection(): com.intellij.codeInspection.InspectionProfileEntry {
    return EslintInspection()
  }

  override fun getGlobalPackageVersionsToInstall(): Map<String, String> {
    return mapOf("eslint" to "9.28.0")
  }

  fun testSuccessfulResponse() {
    val event = collectEvent {
      doEditorHighlightingTest("test.js")
    }

    Assert.assertNotNull("Expected event to be logged", event)
    Assert.assertEquals("js.eslint", event!!.group.id)
    Assert.assertEquals("eslint.response", event.event.id)
    Assert.assertEquals(ESLintEventCollector.ResponseStatus.SUCCESS.toString(), event.event.data["status"])
    Assert.assertTrue("Expected response time to be logged", event.event.data.containsKey("duration_ms"))
  }

  fun testTimeoutResponse() {
    // Deterministic timeout via a never-responding fake service (no real node process spawned). See
    // EslintServiceTestBase.highlightWithNeverRespondingService (WEB-67172).
    myFixture.copyDirectoryToProject(getTestName(false), "")
    val psiFile = myFixture.configureByFile("test.js")
    JSLanguageServiceUtil.setTimeout(1, testRootDisposable)

    val event = collectEvent {
      highlightWithNeverRespondingService(psiFile)
    }

    Assert.assertNotNull("Expected event to be logged", event)
    Assert.assertEquals("js.eslint", event!!.group.id)
    Assert.assertEquals("eslint.response", event.event.id)
    Assert.assertEquals(ESLintEventCollector.ResponseStatus.TIMEOUT.toString(), event.event.data["status"])
    Assert.assertTrue("Expected response time to be logged", event.event.data.containsKey("duration_ms"))
  }

  private fun collectEvent(action: () -> Unit): LogEvent? {
    val events = FUCollectorTestCase.collectLogEvents(testRootDisposable, action)
    return events.singleOrNull { it.group.id == "js.eslint" }
  }
}
