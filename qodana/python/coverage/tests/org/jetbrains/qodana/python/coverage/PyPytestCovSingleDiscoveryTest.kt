package org.jetbrains.qodana.python.coverage

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class PyPytestCovSingleDiscoveryTest(case: Case) : QodanaCoverageDiscoveryTest("PyCoverageInspection", case) {
  @Test
  fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("coverageXmlRoot", ReportLocation("report.xml", "coverage.xml")),
      Case("pytestCoverageName", ReportLocation("report.xml", "pytest-coverage.xml")),
      Case("coverageDir", ReportLocation("report.xml", "coverage/coverage.xml")),
      Case("reportsDir", ReportLocation("report.xml", "reports/cov.xml")),
      Case("coverageReportsDir", ReportLocation("report.xml", "coverage-reports/coverage.xml")),
      Case("dotCoverageReportsDir", ReportLocation("report.xml", ".coverage-reports/coverage.xml")),
      Case("buildDir", ReportLocation("report.xml", "build/arbitary.xml")),
      Case("testResultsDir", ReportLocation("report.xml", "test-results/coverage.xml")),
      Case("targetDir", ReportLocation("report.xml", "target/coverage.xml")),
      Case("artifactsDir", ReportLocation("report.xml", "artifacts/coverage.xml")),
      Case("appsDir", ReportLocation("report.xml", "apps/svc/coverage.xml")),
      Case("packagesDir", ReportLocation("report.xml", "packages/pkg/coverage.xml")),
    )
  }
}
