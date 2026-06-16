package org.jetbrains.qodana.js

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JsLcovSingleDiscoveryTest(case: Case) : QodanaCoverageDiscoveryTest("JsCoverageInspection", case) {
  @Test
  fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("coverageDir", ReportLocation("report.info", "coverage/lcov.info")),
      Case("projectRoot", ReportLocation("report.info", "lcov.info")),
      Case("coverageInfoName", ReportLocation("report.info", "coverage.info")),
      Case("nestedCoverageDir", ReportLocation("report.info", "module/coverage/lcov.info")),
      Case("packagesCoverageDir", ReportLocation("report.info", "packages/pkg/coverage/lcov.info")),
      Case("appsCoverageDir", ReportLocation("report.info", "apps/web/coverage/lcov.info")),
      Case("testCoverageDir", ReportLocation("report.info", "test-coverage/lcov.info")),
      Case("reportsDir", ReportLocation("report.info", "reports/lcov.info")),
      Case("genericInfoName", ReportLocation("report.info", "coverage/results.info")),
      Case("bazelDat", ReportLocation("report.info", "bazel-out/_coverage/coverage.dat")),
    )
  }
}
