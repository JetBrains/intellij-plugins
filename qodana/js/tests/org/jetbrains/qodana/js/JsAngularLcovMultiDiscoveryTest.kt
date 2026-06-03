package org.jetbrains.qodana.js

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test

/**
 * Auto-discovery of multiple LCOV reports from a single project. Mirrors the `js-angular-lcov-multi` sample: Angular
 * CLI writes one `lcov.info` per project under `coverage/<project>/`, each with a project-relative `SF:` path.
 * Discovery must find and merge both.
 */
class JsAngularLcovMultiDiscoveryTest : QodanaCoverageDiscoveryTest("JsCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = listOf(
    ReportLocation("report-app.info", "coverage/app/lcov.info"),
    ReportLocation("report-lib.info", "coverage/lib/lcov.info"),
  )

  @Test
  fun multiProject() = runDiscovery()
}
