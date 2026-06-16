package org.jetbrains.qodana.php

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PhpPhpunitCloverSingleDiscoveryTest(case: Case) : QodanaCoverageDiscoveryTest("PhpCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("cloverBuildLogs", ReportLocation("report.xml", "build/logs/clover.xml")),
      Case("cloverRoot", ReportLocation("report.xml", "clover.xml")),
      Case("phpunitCoverageRoot", ReportLocation("report.xml", "phpunit.coverage.xml")),
      Case("coverageDir", ReportLocation("report.xml", "coverage/coverage.xml")),
      Case("buildCoverageDir", ReportLocation("report.xml", "build/coverage/clover.xml")),
      Case("buildReportsDir", ReportLocation("report.xml", "build/reports/clover.xml")),
      Case("reportsDir", ReportLocation("report.xml", "reports/clover.xml")),
      Case("testReportsDir", ReportLocation("report.xml", "test-reports/clover.xml")),
      Case("testsCoverageDir", ReportLocation("report.xml", "tests/coverage/clover.xml")),
      Case("targetDir", ReportLocation("report.xml", "target/clover.xml")),
    )
  }
}
