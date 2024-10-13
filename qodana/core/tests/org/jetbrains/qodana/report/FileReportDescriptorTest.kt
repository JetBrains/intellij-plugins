package org.jetbrains.qodana.report

import com.google.gson.JsonParseException
import com.jetbrains.qodana.sarif.SarifUtil
import org.jetbrains.qodana.*

class FileReportDescriptorTest : QodanaPluginLightTestBase() {
  override fun runInDispatchThread() = false
  
  fun `test valid sarif success load`() = runDispatchingOnUi {
    val reportPath = sarifTestReports.valid1
    val actualReport = SarifUtil.readReport(reportPath)

    val reportDescriptor = FileReportDescriptor(reportPath, isQodanaReport = true, "REPORT_1", "REPORT_1", project)

    assertTrue(reportDescriptor.checkAvailability())

    assertNoNotifications {
      val loadedReport = reportDescriptor.loadReport(project)?.validatedSarif?.sarif
      assertEquals(actualReport, loadedReport)
    }
  }

  fun `test not existing sarif file`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.notExisting,
      isQodanaReport = true,
      "not existing",
      "not existing",
      project
    )

    assertReportIsAvailableSignalsCount(reportDescriptor, 1) {
      val fileNotExistsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedParsing(ReportParser.FileNotExists))

      assertFalse(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(fileNotExistsMessage) {
        val loadedReport = reportDescriptor.loadReport(project)
        assertNull(loadedReport)
      }
    }
  }

  fun `test sarif with invalid json structure`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.invalidJsonStructure,
      isQodanaReport = true,
      "invalid json",
      "invalid json",
      project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val fileNotExistsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedParsing(ReportParser.JsonParseFailed(
        JsonParseException("Expected BEGIN_OBJECT but was STRING at line 3 column 8 path \$.runs[0]" +
                           "\nSee https://github.com/google/gson/blob/main/Troubleshooting.md#unexpected-json-structure"))))

      assertTrue(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(fileNotExistsMessage) {
        val report = reportDescriptor.loadReport(project)
        assertNull(report)
      }
    }
  }

  fun `test sarif without runs`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.noRuns,
      isQodanaReport = true,
      "no runs",
      "no runs",
      project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noRunsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoRuns))

      assertTrue(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(noRunsMessage) {
        val report = reportDescriptor.loadReport(project)
        assertNull(report)
      }
    }
  }

  fun `test sarif empty runs`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.emptyRuns,
      isQodanaReport = true,
      "empty runs",
      "empty runs",
      project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val emptyRunsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.EmptyRuns))

      assertTrue(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(emptyRunsMessage) {
        val report = reportDescriptor.loadReport(project)
        assertNull(report)
      }
    }
  }

  fun `test sarif without no results`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.noResults,
      isQodanaReport = true,
      "no results",
      "no results",
      project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noResultsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoResults))

      assertTrue(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(noResultsMessage) {
        val report = reportDescriptor.loadReport(project)
        assertNull(report)
      }
    }
  }

  fun `test sarif no tool`() = runDispatchingOnUi {
    val reportDescriptor = FileReportDescriptor(
      sarifTestReports.noTool,
      isQodanaReport = true,
      "no tool",
      "no tool",
      project
    )
    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      val noResultsMessage = reportDescriptor.getNotificationContent(ReportReader.FailedValidation(ReportValidator.NoTool))

      assertTrue(reportDescriptor.checkAvailability())
      assertSingleNotificationWithMessage(noResultsMessage) {
        val report = reportDescriptor.loadReport(project)
        assertNull(report)
      }
    }
  }
}