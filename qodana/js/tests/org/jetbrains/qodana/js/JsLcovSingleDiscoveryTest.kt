package org.jetbrains.qodana.js

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test

class JsLcovSingleDiscoveryTest : QodanaCoverageDiscoveryTest("JsCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "coverageDir" -> listOf(ReportLocation("report.info", "coverage/lcov.info"))
    "projectRoot" -> listOf(ReportLocation("report.info", "lcov.info"))
    "coverageInfoName" -> listOf(ReportLocation("report.info", "coverage.info"))
    else -> error("Unknown test method: $testName")
  }

  @Test
  fun coverageDir() = runDiscovery()

  @Test
  fun projectRoot() = runDiscovery()

  @Test
  fun coverageInfoName() = runDiscovery()
}
