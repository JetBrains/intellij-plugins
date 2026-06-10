package org.jetbrains.qodana.js

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test

class JsAngularLcovMultiDiscoveryTest : QodanaCoverageDiscoveryTest("JsCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = listOf(
    ReportLocation("report-app.info", "coverage/app/lcov.info"),
    ReportLocation("report-lib.info", "coverage/lib/lcov.info"),
  )

  @Test
  fun multiProject() = runDiscovery()
}
