package org.jetbrains.qodana.report

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.problem.revisionId
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.junit.jupiter.api.Test

@TestApplication
class ReportValidatorTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  @Test
  fun `test valid report`() {
    val report = SarifUtil.readReport(qodanaFixture.sarifTestReports.valid1)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat(validatorResult).isInstanceOf(ReportResult.Success::class.java)
    val validatedSarif = (validatorResult as ReportResult.Success).loadedSarifReport

    assertThat(validatedSarif.sarif).isEqualTo(report)
    assertThat(validatedSarif.runs).isNotEmpty
    assertThat(validatedSarif.runsToResults.mapKeys { (run, _) -> run.revisionId() }).isNotNull
    assertThat(validatedSarif.tools).isNotNull
  }

  @Test
  fun `test report without runs`() {
    val report = SarifUtil.readReport(qodanaFixture.sarifTestReports.noRuns)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoRuns)
  }

  @Test
  fun `test report empty runs`() {
    val report = SarifUtil.readReport(qodanaFixture.sarifTestReports.emptyRuns)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.EmptyRuns)
  }

  @Test
  fun `test report no results`() {
    val report = SarifUtil.readReport(qodanaFixture.sarifTestReports.noResults)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoResults)
  }

  @Test
  fun `test report no tool`() {
    val report = SarifUtil.readReport(qodanaFixture.sarifTestReports.noTool)

    val validatorResult = ReportValidator.validateReport(report)

    assertThat((validatorResult as ReportResult.Fail).error).isEqualTo(ReportValidator.NoTool)
  }
}