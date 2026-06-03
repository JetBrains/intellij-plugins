package org.jetbrains.qodana.python.coverage

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test


class PyPytestCovSingleDiscoveryTest : QodanaCoverageDiscoveryTest("PyCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "coverageXml" -> listOf(ReportLocation("report.xml", "coverage.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test
  fun coverageXml() = runDiscovery()
}
