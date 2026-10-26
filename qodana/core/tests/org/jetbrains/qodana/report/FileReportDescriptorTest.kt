package org.jetbrains.qodana.report

import com.google.gson.JsonParseException
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.SarifUtil
import org.jetbrains.qodana.assertNoNotifications
import org.jetbrains.qodana.assertReportIsAvailableSignalsCount
import org.jetbrains.qodana.assertSingleNotificationWithMessage
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.runDispatchingOnUi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class FileReportDescriptorTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  @Test
  fun `test valid sarif success load`(): Unit = runDispatchingOnUi {
    val reportPath = qodanaFixture.sarifTestReports.valid1
    val actualReport = SarifUtil.readReport(reportPath)

    val reportDescriptor = FileReportDescriptor(reportPath, isQodanaReport = true, "REPORT_1", "REPORT_1", qodanaFixture.project)

    assertTrue(reportDescriptor.checkAvailability())

    qodanaFixture.assertNoNotifications {
      val loadedReport = reportDescriptor.loadReport(qodanaFixture.project)?.validatedSarif?.sarif
      assertEquals(actualReport, loadedReport)
    }
  }

  @Test
  fun `test not existing sarif file`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.notExisting,
      isQodanaReport = true,
      "not existing",
      "not existing",
      qodanaFixture.project
    )

    assertReportIsAvailableSignalsCount(reportDescriptor, 1) {
      val fileNotExistsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedParsing(ReportParser.FileNotExists))

      assertFalse(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(fileNotExistsMessage) {
        val loadedReport = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(loadedReport)
      }
    }
  }

  @Test
  fun `test sarif with invalid json structure`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.invalidJsonStructure,
      isQodanaReport = true,
      "invalid json",
      "invalid json",
      qodanaFixture.project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val fileNotExistsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedParsing(ReportParser.JsonParseFailed(
        JsonParseException("Expected BEGIN_OBJECT but was STRING at line 3 column 8 path \$.runs[0]" +
                           "\nSee https://github.com/google/gson/blob/main/Troubleshooting.md#unexpected-json-structure"))))

      assertTrue(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(fileNotExistsMessage) {
        val report = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(report)
      }
    }
  }

  @Test
  fun `test sarif without runs`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.noRuns,
      isQodanaReport = true,
      "no runs",
      "no runs",
      qodanaFixture.project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noRunsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoRuns))

      assertTrue(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(noRunsMessage) {
        val report = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(report)
      }
    }
  }

  @Test
  fun `test sarif empty runs`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.emptyRuns,
      isQodanaReport = true,
      "empty runs",
      "empty runs",
      qodanaFixture.project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val emptyRunsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.EmptyRuns))

      assertTrue(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(emptyRunsMessage) {
        val report = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(report)
      }
    }
  }

  @Test
  fun `test sarif without no results`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.noResults,
      isQodanaReport = true,
      "no results",
      "no results",
      qodanaFixture.project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noResultsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoResults))

      assertTrue(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(noResultsMessage) {
        val report = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(report)
      }
    }
  }

  @Test
  fun `test sarif no tool`(): Unit = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      qodanaFixture.sarifTestReports.noTool,
      isQodanaReport = true,
      "no tool",
      "no tool",
      qodanaFixture.project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noResultsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoTool))

      assertTrue(reportDescriptor.checkAvailability())
      qodanaFixture.assertSingleNotificationWithMessage(noResultsMessage) {
        val report = reportDescriptor.loadReport(qodanaFixture.project)
        assertNull(report)
      }
    }
  }
}