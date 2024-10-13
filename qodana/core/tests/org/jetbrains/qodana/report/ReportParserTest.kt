package org.jetbrains.qodana.report

import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase

class ReportParserTest : QodanaPluginLightTestBase() {
  fun `test valid sarif`() {
    val reportPath = sarifTestReports.valid1
    val actualReport = SarifUtil.readReport(reportPath)

    val parsedReportResult = ReportParser.parseReport(reportPath)

    assertThat((parsedReportResult as ReportResult.Success).loadedSarifReport.sarif).isEqualTo(actualReport)
  }

  fun `test not existing sarif file`() {
    val parsedReportResult = ReportParser.parseReport(sarifTestReports.notExisting)

    assertThat((parsedReportResult as ReportResult.Fail).error).isEqualTo(ReportParser.FileNotExists)
  }

  fun `test sarif with invalid json structure`() {
    val parsedReportResult = ReportParser.parseReport(sarifTestReports.invalidJsonStructure)

    assertThat((parsedReportResult as ReportResult.Fail).error).isInstanceOf(ReportParser.JsonParseFailed::class.java)
  }
}