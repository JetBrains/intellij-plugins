package org.jetbrains.qodana.report

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.junit.jupiter.api.Test

@TestApplication
class ReportParserTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  @Test
  fun `test valid sarif`() {
    val reportPath = qodanaFixture.sarifTestReports.valid1
    val actualReport = SarifUtil.readReport(reportPath)

    val parsedReportResult = ReportParser.parseReport(reportPath)

    assertThat((parsedReportResult as ReportResult.Success).loadedSarifReport.sarif).isEqualTo(actualReport)
  }

  @Test
  fun `test not existing sarif file`() {
    val parsedReportResult = ReportParser.parseReport(qodanaFixture.sarifTestReports.notExisting)

    assertThat((parsedReportResult as ReportResult.Fail).error).isEqualTo(ReportParser.FileNotExists)
  }

  @Test
  fun `test sarif with invalid json structure`() {
    val parsedReportResult = ReportParser.parseReport(qodanaFixture.sarifTestReports.invalidJsonStructure)

    assertThat((parsedReportResult as ReportResult.Fail).error).isInstanceOf(ReportParser.JsonParseFailed::class.java)
  }
}