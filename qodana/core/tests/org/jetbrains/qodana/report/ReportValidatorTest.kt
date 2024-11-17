package org.jetbrains.qodana.report

import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase

class ReportValidatorTest : QodanaPluginLightTestBase() {
  fun `test valid report`() {
    val report = SarifUtil.readReport(sarifTestReports.valid1)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat(validatorResult).isInstanceOf(ReportResult.Success::class.java)
    val validatedSarif = (validatorResult as ReportResult.Success).loadedSarifReport

    assertThat(validatedSarif.sarif).isEqualTo(report)
    assertThat(validatedSarif.runs).isNotEmpty
    assertThat(validatedSarif.revisionsToResults).isNotNull
    assertThat(validatedSarif.tools).isNotNull
  }

  fun `test report without runs`() {
    val report = SarifUtil.readReport(sarifTestReports.noRuns)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoRuns)
  }

  fun `test report empty runs`() {
    val report = SarifUtil.readReport(sarifTestReports.emptyRuns)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.EmptyRuns)
  }

  fun `test report no results`() {
    val report = SarifUtil.readReport(sarifTestReports.noResults)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoResults)
  }

  fun `test report no tool`() {
    val report = SarifUtil.readReport(sarifTestReports.noTool)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoTool)
  }
}